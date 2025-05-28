package ongi.ongibe.domain.album.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FaceCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_picture_id", nullable = false)
    private Picture representativePicture;

    @Column(name = "cluster_name", nullable = false, length = 20)
    private String clusterName;

    @Column(name = "bbox_x1")
    private Integer bboxX1;

    @Column(name = "bbox_y1")
    private Integer bboxY1;

    @Column(name = "bbox_x2")
    private Integer bboxX2;

    @Column(name = "bbox_y2")
    private Integer bboxY2;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
