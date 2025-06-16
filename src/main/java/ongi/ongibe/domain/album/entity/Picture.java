package ongi.ongibe.domain.album.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Slf4j
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 512)
    private String pictureURL;
    @Column(nullable = true)
    private String s3Key;

    @Builder.Default
    @Column(length = 20)
    private String tag = "AI 분석 전";

    @Builder.Default
    private boolean isDuplicated = false;

    @Builder.Default
    private boolean isShaky = false;

    private float qualityScore;

    private LocalDateTime takeAt;
    private Double latitude;
    private Double longitude;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_date", updatable = false) // index용
    private LocalDate createdDate;

    private LocalDateTime deletedAt;

    public AlbumDetailResponseDTO.PictureInfo toPictureInfo() {
        return new AlbumDetailResponseDTO.PictureInfo(
                id,
                pictureURL,
                latitude,
                longitude,
                tag,
                qualityScore,
                isDuplicated,
                isShaky,
                takeAt
        );
    }

    public AlbumSummaryResponseDTO toAlbumSummaryResponseDTO() {
        return new AlbumSummaryResponseDTO(
                id,
                pictureURL,
                latitude,
                longitude
        );
    }

    public UserTotalStateResponseDTO.PictureCoordinate toPictureCoordinate() {
        return new UserTotalStateResponseDTO.PictureCoordinate(
                latitude,
                longitude
        );
    }

    public static Picture of(Album album, User user, String pictureURL) {
        return Picture.builder()
                .album(album)
                .user(user)
                .pictureURL(pictureURL)
                .build();
    }

    public void markAsShaky() {
        if (!this.isShaky) {
            this.isShaky = true;
        }
    }

    public void markAsDuplicate() {
        if (!this.isDuplicated) {
            this.isDuplicated = true;
        }
    }

    public void applyAestheticScore(double score) {
        log.info("[AI] applyAestheticScore 실행됨 - 기존: {}, 새로: {}", this.qualityScore, score);
        this.qualityScore = (float) score;
    }

    public void setTagIfAbsent(String tag) {
        if (this.tag == null || this.tag.isBlank()) {
            this.tag = tag;
        }
    }
}
