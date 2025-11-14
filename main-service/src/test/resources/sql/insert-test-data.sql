-- Вставка тестовых данных для комментариев
INSERT INTO users (id, name, email) VALUES (1, 'Test User', 'test@email.com');
INSERT INTO categories (id, name) VALUES (1, 'Test Category');
INSERT INTO events (id, title, annotation, description, category_id, initiator_id, event_date, created_on, published_on, state, participant_limit, request_moderation, paid, location_lat, location_lon)
VALUES (1, 'Test Event', 'Test Annotation', 'Test Description', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 'PUBLISHED', 0, false, false, 55.7558, 37.6173);
INSERT INTO comments (id, text, author_id, event_id, state, created_on)
VALUES (1, 'Test comment text', 1, 1, 'PENDING', CURRENT_TIMESTAMP);