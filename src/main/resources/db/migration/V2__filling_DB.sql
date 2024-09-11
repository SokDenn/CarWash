insert into role (id, name) values
    ('8bbd529d-4641-449c-a0ef-bed24b65aff1', 'ADMIN'),
    ('2bfac422-3c09-4fd7-951a-41d371c95ac0', 'OPERATOR'),
    ('5d55841f-53be-4c8b-a4ec-07e8d57df049', 'USER');

insert into users (id, username, password, role_id, active) values
    ('7c082c41-b2e8-492f-888f-f3d95bd1ccb6', 'admin', '$2a$08$4Q4/mHP/WknGEtc/fawLhe8a0f5kyGc4OQG.Dj4SAvoGldVELYJRi',
    '8bbd529d-4641-449c-a0ef-bed24b65aff1', true),
    ('76c9280e-ddb5-417e-aa9e-d2118c0fb0ea', 'ivan', '$2a$08$4Q4/mHP/WknGEtc/fawLhe8a0f5kyGc4OQG.Dj4SAvoGldVELYJRi',
    '2bfac422-3c09-4fd7-951a-41d371c95ac0', true),
    ('9d698966-d802-4b96-888a-0e1cef1426dd', 'nik', '$2a$08$4Q4/mHP/WknGEtc/fawLhe8a0f5kyGc4OQG.Dj4SAvoGldVELYJRi',
    '2bfac422-3c09-4fd7-951a-41d371c95ac0', true),
    ('b551ef36-eb55-43fd-b889-720dab646276', 'den', '$2a$08$4Q4/mHP/WknGEtc/fawLhe8a0f5kyGc4OQG.Dj4SAvoGldVELYJRi',
    '5d55841f-53be-4c8b-a4ec-07e8d57df049', true);

insert into washings (id, name, price, duration_minute, is_deleted) values
    ('6465c567-417e-4aef-869e-e9796c0b6344', 'Базовая мойка', 1000, 30, false),
    ('c809c6e1-5797-44ee-92ac-3cfe3e08f3a4', 'Премиум мойка', 2500, 60, false),
    ('a11e813e-4abf-40aa-88c0-3fb9234d36a5', 'Ультра-лакшери мойка', 9999, 120, false);

insert into box (id, box_number, user_operator_id, washing_coefficient, opening_time, closing_time, is_deleted) values
        ('13ab8556-bc6d-4a71-930c-4c1c768b65ff', 1, '76c9280e-ddb5-417e-aa9e-d2118c0fb0ea',
        1.2, '08:00:00', '18:00:00', false),
        ('74646676-e0f3-4cf1-a8e1-38d6f6245762', 20, '9d698966-d802-4b96-888a-0e1cef1426dd',
        0.1, '01:00:00', '22:00:00', false),
        ('154a65c3-5bdf-441f-bb57-27def9e02161', 2, '76c9280e-ddb5-417e-aa9e-d2118c0fb0ea',
        0.9, '08:00:00', '18:00:00', false);

insert into discount (id, min, max) values
        ('7caa0419-00a0-403f-8fb7-aae0c8556986', 0, 30);

INSERT INTO reservation (id, box_id, washing_id, result_price, discount, user_id, start_date_time,
end_date_time, creation_date_time, status, is_deleted) VALUES
('9456bcb4-6b07-41e2-b08b-75b8050b34a2', '13ab8556-bc6d-4a71-930c-4c1c768b65ff', 'a11e813e-4abf-40aa-88c0-3fb9234d36a5',
9999, 0, 'b551ef36-eb55-43fd-b889-720dab646276', '2024-08-02 10:00:00', '2024-08-02 10:30:00', '2024-08-02 10:00:00', 'BOOKED', false),
('c93bcb06-af01-4842-9fb0-922945bc0e6c', '13ab8556-bc6d-4a71-930c-4c1c768b65ff', 'a11e813e-4abf-40aa-88c0-3fb9234d36a5',
9499, 5, 'b551ef36-eb55-43fd-b889-720dab646276', '2024-01-02 10:00:00', '2024-01-02 10:30:00', '2024-01-02 10:00:00', 'AT_CAR_WASH', false),
('b5033615-5c2b-42dd-90d2-4c42bbe75e93', '74646676-e0f3-4cf1-a8e1-38d6f6245762', '6465c567-417e-4aef-869e-e9796c0b6344',
900, 10, 'b551ef36-eb55-43fd-b889-720dab646276', '2024-07-02 10:00:00', '2024-07-02 15:30:00', '2024-07-02 10:00:00', 'AT_CAR_WASH', false),
('e37955ed-a996-4cae-8bb4-8ec7a45d6c6e', '74646676-e0f3-4cf1-a8e1-38d6f6245762', 'c809c6e1-5797-44ee-92ac-3cfe3e08f3a4',
2125, 15, 'b551ef36-eb55-43fd-b889-720dab646276', '2024-05-02 10:00:00', '2024-05-02 22:30:00', '2024-05-02 10:00:00', 'BOOKED', false),
('e698865b-c261-44a6-a22f-a51edd79f645', '154a65c3-5bdf-441f-bb57-27def9e02161', 'c809c6e1-5797-44ee-92ac-3cfe3e08f3a4',
2000, 20, '7c082c41-b2e8-492f-888f-f3d95bd1ccb6', '2023-12-02 11:00:00', '2023-12-02 12:00:00', '2023-12-02 11:00:00', 'AT_CAR_WASH', false);