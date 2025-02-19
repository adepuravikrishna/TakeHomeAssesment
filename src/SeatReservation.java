import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


interface ReservationSystem {
    boolean bookSeat(char row, int seatNumber, int numberOfSeats);

    boolean cancelSeat(char row, int seatNumber, int numberOfSeats);
}


class Seat {
    private final char row;
    private final int seatNumber;
    private boolean isReserved;

    public Seat(char row, int seatNumber) {
        this.row = row;
        this.seatNumber = seatNumber;
        this.isReserved = false;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void reserve() {
        isReserved = true;
    }

    public void cancel() {
        isReserved = false;
    }

    @Override
    public String toString() {
        return isReserved ? "1" : "0";
    }
}


class FileHandler {
    private static final String STATE_FILE = "reservations.txt";

    public static void saveState(Map<Character, Seat[]> seats) {
        try (RandomAccessFile raf = new RandomAccessFile(STATE_FILE, "rw");
             FileChannel channel = raf.getChannel()) {

            FileLock lock = channel.lock();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(raf.getFD()))) {
                for (char row = 'A'; row <= 'T'; row++) {
                    Seat[] rowSeats = seats.get(row);
                    if (rowSeats == null) continue;

                    StringBuilder sb = new StringBuilder();
                    sb.append(row).append(":");
                    for (Seat seat : rowSeats) {
                        sb.append(seat);
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                }
            } finally {
                lock.release();
            }
        } catch (IOException e) {
            // System.out.println("FAIL: Error saving state to file");
        }
    }

    public static Map<Character, Seat[]> loadState() {
        Map<Character, Seat[]> seats = new HashMap<>();
        File file = new File(STATE_FILE);

        if (!file.exists()) {
            // Initialize with empty seats if file doesn't exist
            for (char c = 'A'; c <= 'T'; c++) {
                Seat[] rowSeats = new Seat[8];
                for (int i = 0; i < 8; i++) {
                    rowSeats[i] = new Seat(c, i);
                }
                seats.put(c, rowSeats);
            }
            return seats;
        }

        try (RandomAccessFile raf = new RandomAccessFile(STATE_FILE, "rw");
             FileChannel channel = raf.getChannel()) {

            FileLock lock = channel.lock();
            try (BufferedReader reader = new BufferedReader(new FileReader(raf.getFD()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length != 2 || parts[0].length() != 1) continue;

                    char row = parts[0].charAt(0);
                    String status = parts[1];
                    if (status.length() != 8) continue;

                    Seat[] rowSeats = new Seat[8];
                    for (int i = 0; i < 8; i++) {
                        rowSeats[i] = new Seat(row, i);
                        if (status.charAt(i) == '1') {
                            rowSeats[i].reserve();
                        }
                    }
                    seats.put(row, rowSeats);
                }
            } finally {
                lock.release();
            }
        } catch (IOException e) {
            // System.out.println("FAIL: Error loading state from file");
        }

        return seats;
    }
}


class SeatManager implements ReservationSystem {
    private final Map<Character, Seat[]> seats;
    private final Lock lock = new ReentrantLock();

    public SeatManager() {
        this.seats = FileHandler.loadState();
    }

    @Override
    public boolean bookSeat(char row, int seatNumber, int numberOfSeats) {
        lock.lock();
        try {

            Seat[] rowSeats = seats.get(row);
            if (rowSeats == null) return false;

            for (int i = seatNumber; i < seatNumber + numberOfSeats; i++) {
                if (i == rowSeats.length) return false;
                if (rowSeats[i].isReserved()) return false;
            }

            for (int i = seatNumber; i < seatNumber + numberOfSeats; i++) {
                rowSeats[i].reserve();
            }

            FileHandler.saveState(seats);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean cancelSeat(char row, int seatNumber, int numberOfSeats) {
        lock.lock();
        try {
            Seat[] rowSeats = seats.get(row);
            if (rowSeats == null) return false;

            for (int i = seatNumber; i < seatNumber + numberOfSeats; i++) {
                if (!rowSeats[i].isReserved()) return false;
            }

            for (int i = seatNumber; i < seatNumber + numberOfSeats; i++) {
                rowSeats[i].cancel();
            }

            FileHandler.saveState(seats);
            return true;
        } finally {
            lock.unlock();
        }
    }
}


public class SeatReservation {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("FAIL");
            return;
        }

        String action = args[0].toUpperCase();
        String seatStr = args[1];
        String numStr = args[2];

        char row = seatStr.charAt(0);
        int seatNumber = Integer.parseInt(seatStr.substring(1));
        int numberOfSeats = Integer.parseInt(numStr);

        ReservationSystem reservationSystem = new SeatManager();

        boolean result = false;
        if ("BOOK".equals(action)) {
            result = reservationSystem.bookSeat(row, seatNumber, numberOfSeats);
        } else if ("CANCEL".equals(action)) {
            result = reservationSystem.cancelSeat(row, seatNumber, numberOfSeats);
        }

        System.out.println(result ? "SUCCESS" : "FAIL");
    }
}