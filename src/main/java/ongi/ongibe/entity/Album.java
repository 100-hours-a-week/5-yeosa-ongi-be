package ongi.ongibe.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@SQLDelete(sql = "update album set delete_at = NOW() where id = ?")
@Where(clause = "delete_at is null")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAlbum> userAlbums;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Picture> pictures = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "thumbnail_picture_id")
    private Picture thumbnailPicture;

    private String name;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
