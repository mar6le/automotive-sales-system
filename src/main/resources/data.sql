-- Sample data for Automotive Sales Management System
-- This script will populate the database with initial test data

-- Insert sample vehicles
INSERT INTO vehicles (vin, make, model, year, color, engine_type, transmission, fuel_type, mileage, purchase_price, selling_price, msrp, status, condition_type, purchase_date, description, location, created_at, updated_at, version) VALUES
('1HGBH41JXMN109186', 'Toyota', 'Camry', 2023, 'White', '2.5L I4', 'Automatic', 'Gasoline', 15000, 25000.00, 28000.00, 30000.00, 'AVAILABLE', 'USED', '2024-06-01', 'Well-maintained vehicle with excellent condition', 'Lot A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('1HGBH41JXMN109187', 'Honda', 'Civic', 2023, 'Black', '2.0L I4', 'CVT', 'Gasoline', 8000, 20000.00, 23000.00, 25000.00, 'AVAILABLE', 'USED', '2024-07-15', 'Low mileage, single owner', 'Lot A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('1FTFW1ET5DFC12345', 'Ford', 'F-150', 2022, 'Blue', '3.5L V6', 'Automatic', 'Gasoline', 25000, 35000.00, 38000.00, 42000.00, 'AVAILABLE', 'USED', '2024-05-20', 'Popular pickup truck in great condition', 'Lot B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('1G1ZD5ST8HF123456', 'Chevrolet', 'Malibu', 2024, 'Silver', '1.5L I4 Turbo', 'CVT', 'Gasoline', 5000, 22000.00, 25000.00, 27000.00, 'RESERVED', 'NEW', '2024-08-01', 'Brand new vehicle with latest features', 'Lot A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('WBAJA7C50HWA12345', 'BMW', '3 Series', 2023, 'Red', '2.0L I4 Turbo', 'Automatic', 'Gasoline', 12000, 32000.00, 35000.00, 38000.00, 'AVAILABLE', 'CERTIFIED_PRE_OWNED', '2024-06-10', 'Luxury sedan with premium features', 'Lot C', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('JM1BK32F781234567', 'Mazda', 'CX-5', 2023, 'Gray', '2.5L I4', 'Automatic', 'Gasoline', 18000, 24000.00, 27000.00, 29000.00, 'SOLD', 'USED', '2024-04-15', 'Compact SUV with excellent fuel economy', 'Lot B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('1N4AL3AP8HC123456', 'Nissan', 'Altima', 2024, 'White', '2.5L I4', 'CVT', 'Gasoline', 3000, 21000.00, 24000.00, 26000.00, 'AVAILABLE', 'NEW', '2024-08-10', 'Nearly new with warranty', 'Lot A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('KMHL14JA8HA123456', 'Hyundai', 'Elantra', 2022, 'Blue', '2.0L I4', 'CVT', 'Gasoline', 22000, 18000.00, 21000.00, 23000.00, 'MAINTENANCE', 'USED', '2024-03-20', 'Reliable compact car, currently in service', 'Service Bay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone, date_of_birth, address, city, state, zip_code, country, driver_license, customer_type, company_name, tax_id, credit_score, preferred_contact_method, notes, is_active, created_at, updated_at, version) VALUES
('John', 'Smith', 'john.smith@email.com', '+1234567890', '1985-03-15', '123 Main St', 'Springfield', 'IL', '62701', 'USA', 'S123456789', 'INDIVIDUAL', NULL, NULL, 750, 'EMAIL', 'Excellent credit history', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('Sarah', 'Johnson', 'sarah.johnson@email.com', '+1234567891', '1990-07-22', '456 Oak Ave', 'Chicago', 'IL', '60601', 'USA', 'S987654321', 'INDIVIDUAL', NULL, NULL, 720, 'PHONE', 'First-time buyer', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('Michael', 'Brown', 'michael.brown@company.com', '+1234567892', '1978-11-08', '789 Business Blvd', 'Detroit', 'MI', '48201', 'USA', 'B456789123', 'BUSINESS', 'Brown Enterprises LLC', '12-3456789', 780, 'EMAIL', 'Fleet customer', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('Emily', 'Davis', 'emily.davis@email.com', '+1234567893', '1992-05-30', '321 Elm St', 'Milwaukee', 'WI', '53201', 'USA', 'D789123456', 'INDIVIDUAL', NULL, NULL, 680, 'SMS', 'Young professional', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('Robert', 'Wilson', 'robert.wilson@email.com', '+1234567894', '1975-12-12', '654 Pine Rd', 'Indianapolis', 'IN', '46201', 'USA', 'W321654987', 'INDIVIDUAL', NULL, NULL, 800, 'EMAIL', 'Repeat customer, excellent payment history', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('Lisa', 'Anderson', 'lisa.anderson@fleet.com', '+1234567895', '1983-09-18', '987 Corporate Dr', 'Columbus', 'OH', '43201', 'USA', 'A654987321', 'FLEET', 'Anderson Fleet Services', '98-7654321', 760, 'EMAIL', 'Large fleet customer', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('David', 'Martinez', 'david.martinez@email.com', '+1234567896', '1988-02-14', '147 Sunset Blvd', 'Phoenix', 'AZ', '85001', 'USA', 'M147258369', 'INDIVIDUAL', NULL, NULL, 710, 'PHONE', 'Interested in electric vehicles', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('Jennifer', 'Taylor', 'jennifer.taylor@email.com', '+1234567897', '1995-06-25', '258 River St', 'Nashville', 'TN', '37201', 'USA', 'T258369147', 'INDIVIDUAL', NULL, NULL, 690, 'EMAIL', 'Recent graduate', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Insert sample sales
INSERT INTO sales (vehicle_id, customer_id, sale_date, sale_price, down_payment, trade_in_value, financing_amount, interest_rate, loan_term_months, monthly_payment, payment_method, sale_status, salesperson_name, salesperson_email, commission_rate, commission_amount, warranty_months, extended_warranty, extended_warranty_cost, delivery_date, delivery_address, notes, contract_signed_at, is_finalized, created_at, updated_at, version) VALUES
(6, 1, '2024-08-15', 27000.00, 5000.00, 0.00, 22000.00, 4.5, 60, 410.00, 'FINANCING', 'COMPLETED', 'Alex Thompson', 'alex.thompson@automotive.com', 2.5, 675.00, 36, false, 0.00, '2024-08-20', '123 Main St, Springfield, IL 62701', 'Customer very satisfied with purchase', '2024-08-15 14:30:00', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 2, '2024-08-20', 25000.00, 3000.00, 8000.00, 14000.00, 3.9, 48, 318.00, 'COMBINATION', 'APPROVED', 'Maria Rodriguez', 'maria.rodriguez@automotive.com', 2.0, 500.00, 24, true, 1500.00, '2024-08-25', '456 Oak Ave, Chicago, IL 60601', 'Trade-in processed smoothly', NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1, 5, '2024-08-10', 28000.00, 10000.00, 0.00, 18000.00, 4.2, 48, 410.00, 'FINANCING', 'COMPLETED', 'James Wilson', 'james.wilson@automotive.com', 3.0, 840.00, 48, true, 2000.00, '2024-08-12', '654 Pine Rd, Indianapolis, IN 46201', 'Repeat customer, smooth transaction', '2024-08-10 16:45:00', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Update vehicle statuses based on sales
UPDATE vehicles SET status = 'SOLD' WHERE id IN (1, 6);
UPDATE vehicles SET status = 'RESERVED' WHERE id = 4;
