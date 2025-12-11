-- Создание таблицы t_role (Роли)
CREATE TABLE t_role (
                        id BIGINT PRIMARY KEY,
                        name CHARACTER VARYING
);

-- Создание таблицы t_user (Пользователи)
CREATE TABLE t_user (
                        id BIGINT PRIMARY KEY,
                        username CHARACTER VARYING NOT NULL,
                        password CHARACTER VARYING NOT NULL,
                        email CHARACTER VARYING NOT NULL,
                        first_name CHARACTER VARYING NOT NULL,
                        last_name CHARACTER VARYING NOT NULL,
                        phone CHARACTER VARYING NOT NULL,
                        busyness CHARACTER VARYING
);

-- Создание таблицы t_task (Задачи)
CREATE TABLE t_task (
                        id BIGINT PRIMARY KEY,
                        name CHARACTER VARYING NOT NULL,
                        description CHARACTER VARYING,
                        task_type CHARACTER VARYING,
                        task_purpose CHARACTER VARYING,
                        order_id BIGINT,
                        user_id BIGINT
);

-- Создание таблицы связи t_user_roles (Многие-ко-многим: пользователи и роли)
CREATE TABLE t_user_roles (
                              user_id BIGINT NOT NULL,
                              roles_id BIGINT NOT NULL,
                              PRIMARY KEY (user_id, roles_id)
);

-- Добавление внешних ключей для таблицы t_task
ALTER TABLE t_task
    ADD CONSTRAINT fk_task_user
        FOREIGN KEY (user_id)
            REFERENCES t_user(id);

-- Добавление внешних ключей для таблицы t_user_roles
ALTER TABLE t_user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES t_user(id);

ALTER TABLE t_user_roles
    ADD CONSTRAINT fk_user_roles_role
        FOREIGN KEY (roles_id)
            REFERENCES t_role(id);