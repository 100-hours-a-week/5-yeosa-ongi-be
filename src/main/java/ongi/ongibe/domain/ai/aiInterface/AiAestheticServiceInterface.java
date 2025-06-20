package ongi.ongibe.domain.ai.aiInterface;

import java.util.List;

public interface AiAestheticServiceInterface {

    void requestAestheticScores(Long albumId, Long userId, List<String> s3keys);

}
