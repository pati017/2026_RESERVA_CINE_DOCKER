CREATE TABLE movies (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    director VARCHAR(255) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    duration_min INT,
    rating VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE screenings (
    id UUID PRIMARY KEY,
    movie_id UUID NOT NULL,
    room VARCHAR(50) NOT NULL,
    show_at TIMESTAMP WITH TIME ZONE NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL CHECK (available_seats >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);
