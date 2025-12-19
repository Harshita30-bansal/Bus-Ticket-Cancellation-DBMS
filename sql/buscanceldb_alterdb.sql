-- Add Foreign Key Constraints to the Bookings Table
ALTER TABLE Bookings
    ADD CONSTRAINT fk_bus_id FOREIGN KEY (Bus_ID) REFERENCES Buses(Bus_ID),
    ADD CONSTRAINT fk_passenger_id FOREIGN KEY (Passenger_ID) REFERENCES Passengers(Passenger_ID);

-- Add Foreign Key Constraints to the Cancellations Table
ALTER TABLE Cancellations
    ADD CONSTRAINT fk_booking_id FOREIGN KEY (Booking_ID) REFERENCES Bookings(Booking_ID);

-- Add Foreign Key Constraints to the Refunds Table
ALTER TABLE Refunds
    ADD CONSTRAINT fk_cancellation_id FOREIGN KEY (Cancellation_ID) REFERENCES Cancellations(Cancellation_ID);

-- Add Foreign Key Constraints to the Notifications Table
ALTER TABLE Notifications
    ADD CONSTRAINT fk_booking_id_notification FOREIGN KEY (Booking_ID) REFERENCES Bookings(Booking_ID);
