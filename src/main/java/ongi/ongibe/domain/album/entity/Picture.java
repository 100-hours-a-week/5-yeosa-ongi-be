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
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@SQLDelete(sql = "UPDATE picture SET deleted_at = NOW() WHERE id = ?")
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

    @Column(length = 8)
    private String tag;
    private boolean isDuplicated = false;
    private boolean isShaky = false;
    private float qualityScore;

    private LocalDateTime takeAt;
    private double latitude;
    private double longitude;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public AlbumDetailResponseDTO.PictureInfo toPictureInfo(){
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

    public AlbumSummaryResponseDTO toAlbumSummaryResponseDTO(){
        return new AlbumSummaryResponseDTO(
                id,
                pictureURL,
                latitude,
                longitude
        );
    }

    public UserTotalStateResponseDTO.PictureCoordinate toPictureCoordinate(){
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
}
