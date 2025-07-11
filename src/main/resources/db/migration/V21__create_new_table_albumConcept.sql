create table album_concept(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concept VARCHAR(255),
    album_id BIGINT NOT NULL,

    CONSTRAINT fk_concept_album
                          foreign key (album_id)
                          references album(id)
)