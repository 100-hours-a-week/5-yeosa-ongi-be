package ongi.ongibe.domain.album.dto;

import java.util.List;

public record AlbumPictureUpdateRequestDTO(
   List<Long> pictureIds
) {}
