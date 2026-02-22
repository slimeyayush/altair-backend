-- data.sql

INSERT INTO products (name, description, price, old_price, stock_quantity, category, tag, image_url) VALUES
('BMC RESmart GII Auto CPAP', 'Standard Auto CPAP Machine with Humidifier for Sleep Apnea Therapy.', 18490.00, 35000.00, 15, 'CPAP Devices', 'Hot', 'https://placehold.co/400x400/1e293b/3b82f6?text=Auto+CPAP'),

('Yuwell YN-03 Nasal CPAP Mask', 'Premium Nasal CPAP Mask – Magnet-Free, Comfortable & Secure.', 1990.00, 3500.00, 50, 'Masks', 'New', 'https://placehold.co/400x400/1e293b/3b82f6?text=Nasal+Mask'),

('High-Efficiency HEPA Filter', 'Air Inlet HEPA Filter for Philips Oxygen Concentrator.', 249.00, 400.00, 200, 'Accessories', 'Sale', 'https://placehold.co/400x400/1e293b/3b82f6?text=HEPA+Filter'),

('Coolnut 24000mAh Power Bank', 'High-capacity Power Bank designed specifically for CPAP & BIPAP Devices.', 11490.00, 19000.00, 10, 'Accessories', NULL, 'https://placehold.co/400x400/1e293b/3b82f6?text=Power+Bank'),

('Resmed AirSense 10 Autoset Tripack', 'Premium Auto CPAP Machine with integrated humidifier and advanced event detection.', 47700.00, 87990.00, 8, 'CPAP Devices', 'Best Seller', 'https://placehold.co/400x400/1e293b/3b82f6?text=AirSense+10'),

('BMC Y30T BIPAP Machine', 'Advanced BIPAP device providing dual-level positive airway pressure.', 23490.00, 70000.00, 12, 'BiPAP Devices', 'Hot', 'https://placehold.co/400x400/1e293b/3b82f6?text=BiPAP+Machine');

INSERT INTO admin_users (username, password, role) VALUES ('master_admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCG3JZGZ3X.HCG3JZGZ3X', 'ADMIN');