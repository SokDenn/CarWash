create table discount (
    id UUID PRIMARY KEY,
    min INTEGER NOT NULL,
    max INTEGER NOT NULL
);

create table role (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

create table users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id UUID,
    active BOOLEAN NOT NULL,
    CONSTRAINT users_role_fk FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE UNIQUE INDEX unique_active_username ON users(username) WHERE active = true;

create table box (
    id UUID PRIMARY KEY,
    box_number INTEGER,
    user_operator_id UUID NOT NULL,
    washing_coefficient DECIMAL(10, 2) NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    CONSTRAINT box_user_fk FOREIGN KEY (user_operator_id) REFERENCES users(id)
);

create table washings (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL,
    duration_minute INTEGER NOT NULL,
    is_deleted BOOLEAN NOT NULL
);

create table reservation (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL,
    washing_id UUID NOT NULL,
    result_price INTEGER NOT NULL,
    discount INTEGER NOT NULL,
    user_id UUID,
    start_date_time TIMESTAMP,
    end_date_time TIMESTAMP,
    creation_date_time TIMESTAMP,
    status VARCHAR(255) CHECK (status IN ('WAITING_RESERVATION', 'BOOKED', 'AT_CAR_WASH', 'CANCELLED', 'COMPLETED')),
    is_deleted BOOLEAN NOT NULL,
    CONSTRAINT reservation_box_fk FOREIGN KEY (box_id) REFERENCES box(id),
    CONSTRAINT reservation_washing_fk FOREIGN KEY (washing_id) REFERENCES washings(id),
    CONSTRAINT reservation_user_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

