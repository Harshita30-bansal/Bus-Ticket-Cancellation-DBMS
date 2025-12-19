-- Create the BusCancelDB Database
CREATE DATABASE IF NOT EXISTS buscanceldb;

-- Use the created database
USE buscanceldb;

-- Create the Buses Table
CREATE TABLE Buses (
    Bus_ID INT PRIMARY KEY,
    Bus_Name VARCHAR(100),
    Route VARCHAR(100)
);

-- Create the Passengers Table
CREATE TABLE Passengers (
    Passenger_ID INT PRIMARY KEY,
    Name VARCHAR(100),
    Contact_Info VARCHAR(100),
    Email VARCHAR(100)
);

-- Create the Bookings Table
CREATE TABLE Bookings (
    Booking_ID INT PRIMARY KEY,
    Bus_ID INT,
    Passenger_ID INT,
    Booking_Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Status ENUM('Booked', 'Cancelled', 'Refunded') DEFAULT 'Booked'
);

-- Create the Cancellations Table
CREATE TABLE Cancellations (
    Cancellation_ID INT PRIMARY KEY AUTO_INCREMENT,
    Booking_ID INT,
    Cancellation_Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Cancellation_Reason VARCHAR(255),
    Refund_Status ENUM('Pending', 'Processed') DEFAULT 'Pending'
);

-- Create the Refunds Table
CREATE TABLE Refunds (
    Refund_ID INT PRIMARY KEY AUTO_INCREMENT,
    Cancellation_ID INT,
    Refund_Amount DECIMAL(10, 2),
    Refund_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the Notifications Table
CREATE TABLE Notifications (
    Notification_ID INT PRIMARY KEY AUTO_INCREMENT,
    Booking_ID INT,
    Notification_Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Message VARCHAR(255)
);

-- Create the Cancellation Reports Table
CREATE TABLE Cancellation_Reports (
    Report_ID INT PRIMARY KEY AUTO_INCREMENT,
    Report_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Total_Cancellations INT,
    Total_Refunded DECIMAL(10, 2)
);
