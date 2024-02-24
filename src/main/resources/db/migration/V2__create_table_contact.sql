CREATE TABLE IF NOT EXISTS contact(
    id UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    phone VARCHAR,
    category_id UUID,

    FOREIGN KEY(category_id) REFERENCES category(id)
)