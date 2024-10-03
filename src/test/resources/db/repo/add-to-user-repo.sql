DELETE FROM users;
INSERT INTO users (id, email, first_name, last_name, password, role, deleted)
VALUES (1, 'admin@simpleart.eu', 'B', 'C', 'password', 'MANAGER', false);
