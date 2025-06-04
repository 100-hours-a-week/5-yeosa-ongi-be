package ongi.ongibe.domain.album.event;

import java.util.List;

public record PictureStatChangeEvent(String yearMonth, List<Long> memberId) {

}
