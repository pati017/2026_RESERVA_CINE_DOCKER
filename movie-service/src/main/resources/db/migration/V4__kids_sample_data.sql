-- Datos de muestra infantiles. Se actualizan las MISMAS filas de V2 (mismos UUID y capacidades),
-- así que funciona tanto en una BD nueva como en una ya migrada, y los IDs de Postman siguen válidos.
UPDATE movies SET title = 'Toy Story', director = 'John Lasseter', genre = 'ANIMATION', duration_min = 81,  rating = 'G'
 WHERE id = 'd508494b-4b2a-431c-99d9-bb4fc27de754';

UPDATE movies SET title = 'Coco', director = 'Lee Unkrich', genre = 'ANIMATION', duration_min = 105, rating = 'PG'
 WHERE id = 'a2c3a51f-632b-4cd3-a8cd-0efea7ceeb2d';

UPDATE screenings SET room = 'Sala Infantil', show_at = '2026-10-10 15:00:00'
 WHERE id = '73d2a76f-005a-4b96-b072-cd414e5b22b6';   -- Toy Story, 45 asientos

UPDATE screenings SET room = 'Sala 2', show_at = '2026-10-10 17:00:00'
 WHERE id = 'c8465057-2e8c-4a34-a63e-3296c0d655f9';   -- Toy Story, 100 asientos

UPDATE screenings SET room = 'Sala 1', show_at = '2026-10-11 16:00:00'
 WHERE id = 'b9e38d7c-87d5-45cf-b0d7-ebec9547d7c6';   -- Coco, 150 asientos
