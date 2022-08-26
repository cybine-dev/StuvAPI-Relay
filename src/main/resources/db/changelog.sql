-- liquibase formatted sql

-- changeset fboelter:1
-- comment initial setup
-- rollback
--      DROP TABLE rooms,
--      DROP TABLE lectures,
--      DROP TABLE lecture_rooms,
--      DROP TABLE syncs,
--      DROP TABLE sync_data;
CREATE TABLE rooms
(
    id           BINARY(16)   NOT NULL,
    name         VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT Rooms_Name_SK UNIQUE KEY (name)
);

CREATE TABLE lectures
(
    id         BINARY(16)   NOT NULL,
    lecture_id BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    starts_at  DATETIME     NOT NULL,
    ends_at    DATETIME     NOT NULL,
    type       TINYINT      NOT NULL,
    course     VARCHAR(64),
    lecturer   TEXT,
    PRIMARY KEY (id),
    CONSTRAINT Lectures_LectureId_SK UNIQUE KEY (lecture_id)
);

CREATE TABLE lecture_rooms
(
    lecture_id BINARY(16) NOT NULL,
    room_id    BINARY(16) NOT NULL,
    CONSTRAINT LectureRooms_LectureId_FK FOREIGN KEY (lecture_id) REFERENCES lectures (id) ON DELETE CASCADE,
    CONSTRAINT LectureRooms_RoomId_FK FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);

CREATE TABLE syncs
(
    id          BINARY(16) NOT NULL,
    started_at  DATETIME   NOT NULL,
    finished_at DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE sync_data
(
    id         BINARY(16) NOT NULL,
    sync_id    BINARY(16) NOT NULL,
    lecture_id BINARY(16),
    type       TINYINT,
    data       JSON,
    PRIMARY KEY (id),
    CONSTRAINT SyncData_SyncId_FK FOREIGN KEY (sync_id) REFERENCES syncs (id) ON DELETE CASCADE
);
