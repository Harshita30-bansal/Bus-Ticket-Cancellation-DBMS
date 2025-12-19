-- Insert initial data into Buses Table
INSERT INTO Buses (Bus_ID, Bus_Name, Route) VALUES
(1, 'Bus A', 'Route 1'),
(2, 'Bus B', 'Route 2'),
(3, 'Bus C', 'Route 3'),
(4, 'Bus D', 'Route 4');

-- Insert initial data into Passengers Table
INSERT INTO Passengers (Passenger_ID, Name, Contact_Info, Email) VALUES
(1001, 'Alice Smith', '1234567890', 'alice.smith@example.com'),
(1002, 'Bob Johnson', '2345678901', 'bob.johnson@example.com'),
(1003, 'Charlie Brown', '3456789012', 'charlie.brown@example.com'),
(1004, 'Diana White', '4567890123', 'diana.white@example.com');

-- Insert initial data into Bookings Table
INSERT INTO Bookings (Booking_ID, Bus_ID, Passenger_ID, Booking_Time, Status) VALUES
(1, 1, 1001, '2025-04-26 08:30:00', 'Booked'),
(2, 2, 1002, '2025-04-26 09:00:00', 'Booked'),
(3, 3, 1003, '2025-04-26 09:30:00', 'Booked'),
(4, 4, 1004, '2025-04-26 10:00:00', 'Booked');

-- No cancellations or refunds at the beginning
-- Keep Cancellations, Refunds, Notifications, Cancellation_Reports EMPTY

-- (Optional) If you want to clear old data before inserting fresh:
-- DELETE FROM Cancellations;
-- DELETE FROM Refunds;
-- DELETE FROM Notifications;
-- DELETE FROM Cancellation_Reports;
