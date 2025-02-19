# Flight Reservation

This project allows you to reserve seats in flight. Rervations.txt will be generated when we initially run the program. We only need SeatReservation.java file to run test everything. For readability all other calsses, interfaces are included in same file.
## Installation
Below is the output produced by the program
```sh
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A0 1
SUCCESS
ravikrishnaadepu@Mac src % java SeatReservation.java CANCEL A0 1
SUCCESS
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A0 1  
SUCCESS
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A0 1
FAIL
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A1 1
SUCCESS
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A2 4  
SUCCESS
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A5 1
FAIL
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A6 3
FAIL
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK A8 1
FAIL
ravikrishnaadepu@Mac src % java SeatReservation.java BOOK U1 1
FAIL

