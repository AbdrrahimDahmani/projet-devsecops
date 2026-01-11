-- Initialisation de la base de données produits
-- Ce script est exécuté automatiquement au démarrage de PostgreSQL

-- Insertion de données de test
INSERT INTO products (nom, description, prix, quantite_stock, actif, created_at, updated_at)
VALUES 
    ('Laptop HP ProBook', 'Ordinateur portable professionnel 15.6 pouces, Intel i7, 16Go RAM, 512Go SSD', 899.99, 50, true, NOW(), NOW()),
    ('Souris Logitech MX Master 3', 'Souris sans fil ergonomique pour productivité', 99.99, 200, true, NOW(), NOW()),
    ('Clavier Mécanique Keychron K2', 'Clavier mécanique sans fil compact, switches Brown', 79.99, 150, true, NOW(), NOW()),
    ('Écran Dell 27 pouces 4K', 'Moniteur professionnel UltraSharp 4K USB-C', 549.99, 30, true, NOW(), NOW()),
    ('Webcam Logitech C920', 'Webcam Full HD 1080p avec microphone intégré', 69.99, 100, true, NOW(), NOW()),
    ('Casque Sony WH-1000XM4', 'Casque sans fil à réduction de bruit active', 279.99, 75, true, NOW(), NOW()),
    ('Hub USB-C Anker', 'Hub 7-en-1 USB-C avec HDMI, USB 3.0, lecteur SD', 49.99, 250, true, NOW(), NOW()),
    ('SSD Samsung 1To', 'Disque SSD NVMe M.2 haute performance', 119.99, 100, true, NOW(), NOW()),
    ('Station d''accueil Lenovo', 'Station d''accueil ThinkPad USB-C', 199.99, 40, true, NOW(), NOW()),
    ('Tablette Graphique Wacom', 'Tablette graphique Intuos Pro Medium', 349.99, 25, true, NOW(), NOW())
ON CONFLICT DO NOTHING;
