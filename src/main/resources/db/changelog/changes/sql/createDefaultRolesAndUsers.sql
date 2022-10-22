BEGIN;
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_USER');

INSERT INTO "user" (name, username, password) VALUES ('admin', 'admin', '$2a$10$QAjxjsOumHJ1FmLRxwF8Ze377MQa.JBffYqmRpD8.dU0hO/11/lf.');
INSERT INTO "user" (name, username, password) VALUES ('demo', 'demo', '$2a$10$QeO9IG7iUkICeT3FdZTnC.Qy80bPQxqMfQqiFWph9c8yP6u/JU6Wq');

INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT id FROM "user" WHERE name = 'admin'), (SELECT id FROM role WHERE name = 'ROLE_ADMIN')),
       ((SELECT id FROM "user" WHERE name = 'admin'), (SELECT id FROM role WHERE name = 'ROLE_USER')),
       ((SELECT id FROM "user" WHERE name = 'demo'), (SELECT id FROM role WHERE name = 'ROLE_USER'));

COMMIT;