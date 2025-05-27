-- 얼굴 클러스터 테이블
CREATE TABLE face_cluster (
                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              representative_picture_id BIGINT NOT NULL,
                              cluster_name VARCHAR(20) NOT NULL,
                              bbox_x1 INT,
                              bbox_y1 INT,
                              bbox_x2 INT,
                              bbox_y2 INT,
                              created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),

                              CONSTRAINT fk_facecluster_picture
                                  FOREIGN KEY (representative_picture_id)
                                      REFERENCES picture(id)
                                      ON DELETE CASCADE
);

-- 사진-클러스터 중간 테이블
CREATE TABLE picture_face_cluster (
                                      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      picture_id BIGINT NOT NULL,
                                      face_cluster_id BIGINT NOT NULL,

                                      CONSTRAINT fk_pfc_picture
                                          FOREIGN KEY (picture_id)
                                              REFERENCES picture(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_pfc_facecluster
                                          FOREIGN KEY (face_cluster_id)
                                              REFERENCES face_cluster(id)
                                              ON DELETE CASCADE
);