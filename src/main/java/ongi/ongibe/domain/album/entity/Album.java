package ongi.ongibe.domain.album.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO.PictureInfo;
import ongi.ongibe.domain.album.event.AlbumNameChangedEvent;
import ongi.ongibe.domain.album.event.AlbumThumbnailChangedEvent;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;
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

    public void changeName(String newName) {
        if (!Objects.equals(this.name, newName)) {
            this.name = newName;
            domainEvent.add(new AlbumNameChangedEvent(newName));
        }
    }

    public void changeThumbnailPicture(Picture thumbnailPicture) {
        if (!Objects.equals(this.thumbnailPicture, thumbnailPicture)) {
            this.thumbnailPicture = thumbnailPicture;
            domainEvent.add(new AlbumThumbnailChangedEvent(thumbnailPicture));
        }
    }


    public void changeProcessState(AlbumProcessState newState) {
        if (this.processState != newState) {
            this.processState = newState;
            domainEvent.add(new AlbumProcessStateChangedEvent(this));
        }
    }
}
