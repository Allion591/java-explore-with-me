INSERT INTO users (id, name, email, created_on)
VALUES (1, 'Test User', 'test@email.com', CURRENT_TIMESTAMP);

INSERT INTO categories (id, name)
VALUES (1, 'Test Category');

INSERT INTO events (id, annotation, category_id, description, event_date, lat, lon, paid, participant_limit, request_moderation, title, initiator_id, state, created_on)
VALUES (1, 'Test Annotation', 1, 'Test Description', CURRENT_TIMESTAMP, 55.7558, 37.6173, false, 0, false, 'Test Event', 1, 'PUBLISHED', CURRENT_TIMESTAMP);

INSERT INTO comments (comments_id, text, event_id, author_id, created_date, updated_date, state)
VALUES (1, 'Test comment text', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING');