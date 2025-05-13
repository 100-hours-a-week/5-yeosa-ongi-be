package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlbumOwnerTransferResponseDTO(
        @Schema(description = "기존 앨범 소유자 유저id") Long oldOwnerId,
        @Schema(description = "새 앨범 소유자 유저id")Long newOwnerId
) {

}
