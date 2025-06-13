package ongi.ongibe.domain.album.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "update picture_face_cluster set deleted_at = NOW() where id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PictureFaceCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picture_id", nullable = false)
    private Picture picture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_cluster_id", nullable = false)
    private FaceCluster faceCluster;

    private LocalDateTime deletedAt;
}