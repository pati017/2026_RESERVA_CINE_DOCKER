-- Insertar 2 peliculas
INSERT INTO movies (id, title, director, genre, duration_min, rating, active, created_at, updated_at) 
VALUES 
('d508494b-4b2a-431c-99d9-bb4fc27de754', 'Dune: Part Two', 'Denis Villeneuve', 'SCI_FI', 166, 'PG-13', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a2c3a51f-632b-4cd3-a8cd-0efea7ceeb2d', 'Oppenheimer', 'Christopher Nolan', 'DRAMA', 180, 'R', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar 3 funciones
INSERT INTO screenings (id, movie_id, room, show_at, total_seats, available_seats, active) 
VALUES 
('73d2a76f-005a-4b96-b072-cd414e5b22b6', 'd508494b-4b2a-431c-99d9-bb4fc27de754', 'Sala IMAX', '2026-10-10 20:00:00+00', 45, 45, TRUE),
('c8465057-2e8c-4a34-a63e-3296c0d655f9', 'd508494b-4b2a-431c-99d9-bb4fc27de754', 'Sala 2', '2026-10-10 22:30:00+00', 100, 100, TRUE),
('b9e38d7c-87d5-45cf-b0d7-ebec9547d7c6', 'a2c3a51f-632b-4cd3-a8cd-0efea7ceeb2d', 'Sala 1', '2026-10-11 19:00:00+00', 150, 150, TRUE);
