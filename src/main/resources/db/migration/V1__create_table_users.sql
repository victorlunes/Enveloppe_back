CREATE TABLE elp_users
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    birthday DATE,
    password VARCHAR(255) NOT NULL
);