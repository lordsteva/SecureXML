
INSERT INTO authority (id, name) VALUES (1, 'ROLE_SYSTEM_ADMIN');

INSERT INTO users (id, activated,email, password, username) VALUES (1,true, 'admin@admin.admin','$2a$10$BjZ8Ihb955YG9AiR4JvkL.k9WUR7/oGa8LIF6Q0ys7k.EVrbOtRqe','admin');

INSERT INTO user_authority (user_id, authority_id) VALUES (1, 1);