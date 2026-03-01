-- Seed 1 questionnaire pour MVP
INSERT INTO questionnaires (name, version, status)
VALUES ('MVP Questionnaire3', 3, 'published')
ON CONFLICT (name, version) DO NOTHING;

INSERT INTO Users (email, password_hash, role)
VALUES ('admin@example.com','$2a$10$5eqJKF67z1ply2w/rqUtoup2OEJxZMlOVRf3qH9PvELsOP0ij0GXS', 'admin');
