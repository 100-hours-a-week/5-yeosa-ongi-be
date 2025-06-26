package ongi.ongibe.domain.album.dto;

public record AlbumCommentsResponseDTO(
        String userName,
        String userProfile,
        String content,
        String createdAt
) {}
