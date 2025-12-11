
CREATE TABLE t_role (
    id BIGINT PRIMARY KEY,
    name CHARACTER VARYING
);

CREATE TABLE t_user (
    id BIGINT PRIMARY KEY,
    username CHARACTER VARYING NOT NULL,
    password CHARACTER VARYING NOT NULL,
    email CHARACTER VARYING NOT NULL,
    first_name CHARACTER VARYING NOT NULL,
    last_name CHARACTER VARYING NOT NULL,
    phone CHARACTER VARYING NOT NULL
);

CREATE TABLE t_user_roles (
    user_id BIGINT NOT NULL,
    roles_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, roles_id)
);

CREATE TABLE t_product (
    id BIGINT PRIMARY KEY,
    name CHARACTER VARYING,
    material CHARACTER VARYING,
    cost DOUBLE PRECISION,
    quantity INTEGER,
    image CHARACTER VARYING,
    gender VARCHAR(10),
    clothes_type VARCHAR(50)
);

CREATE TABLE t_order (
    id BIGINT PRIMARY KEY,
    address CHARACTER VARYING,
    amount DOUBLE PRECISION,
    quantity INTEGER,
    status CHARACTER VARYING,
    user_id BIGINT NOT NULL
);

CREATE TABLE t_cart (
    id BIGINT PRIMARY KEY,
    amount DOUBLE PRECISION,
    product_quantity INTEGER NOT NULL,
    status CHARACTER VARYING,
    order_id BIGINT,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

ALTER TABLE t_order
    ADD FOREIGN KEY (user_id)
        REFERENCES t_user(id);

ALTER TABLE t_cart
    ADD FOREIGN KEY (order_id)
        REFERENCES t_order(id);

ALTER TABLE t_cart
    ADD FOREIGN KEY (product_id)
        REFERENCES t_product(id);

ALTER TABLE t_cart
    ADD FOREIGN KEY (user_id)
        REFERENCES t_user(id);

ALTER TABLE t_user_roles
    ADD FOREIGN KEY (user_id)
        REFERENCES t_user(id);

ALTER TABLE t_user_roles
    ADD FOREIGN KEY (roles_id)
        REFERENCES t_role(id);