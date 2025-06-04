package ongi.ongibe.domain.album.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.event.AlbumNameChangeEvent;
import ongi.ongibe.domain.album.event.AlbumPictureAddEvent;
import ongi.ongibe.domain.album.event.AlbumProcessStateChangeEvent;
import ongi.ongibe.domain.album.event.AlbumThumbnailChangeEvent;
import ongi.ongibe.domain.album.event.PictureStatChangeEvent;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.util.DateUtil;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@SQLDelete(sql = "update album set deleted_at = NOW() where id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAlbum> userAlbums = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Picture> pictures = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "thumbnail_picture_id")
    private Picture thumbnailPicture;

    @Column(length = 12)
    private String name;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlbumProcessState processState = AlbumProcessState.NOT_STARTED;

    @Transient
    private final List<Object> domainEvent = new ArrayList<>();

    @DomainEvents
    public List<Object> domainEvents() {
        return domainEvent;
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvent.clear();
    }

    public void changeName(String newName, List<Long> memberIds) {
        if (!Objects.equals(this.name, newName)) {
            this.name = newName;
            domainEvent.add(new AlbumNameChangeEvent(DateUtil.getYearMonth(this.createdAt), memberIds));
        }
    }

    public void changeThumbnailPicture(Picture thumbnailPicture, List<Long> memberIds) {
        if (!Objects.equals(this.thumbnailPicture, thumbnailPicture)) {
            this.thumbnailPicture = thumbnailPicture;
            domainEvent.add(new AlbumThumbnailChangeEvent(DateUtil.getYearMonth(this.createdAt), memberIds));
        }
    }


    public void changeProcessState(AlbumProcessState newState, List<Long> memberIds) {
        if (this.processState != newState) {
            this.processState = newState;
            domainEvent.add(new AlbumProcessStateChangeEvent(DateUtil.getYearMonth(this.createdAt), memberIds));
        }
    }

    public void addPictures(List<Picture> newPictures, User user, List<Long> memberIds) {
        this.pictures.addAll(newPictures);
        domainEvent.add(new AlbumPictureAddEvent(DateUtil.getYearMonth(this.createdAt), memberIds));
        domainEvent.add(new PictureStatChangeEvent(DateUtil.getYearMonth(this.createdAt), memberIds));
    }
}
