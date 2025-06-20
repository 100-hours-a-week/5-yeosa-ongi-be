package ongi.ongibe.domain.ai.aiInterface;

import java.util.List;

public interface AiClusterServiceInterface {

    void requestCluster(Long albumId, Long userId, List<String> s3keys)
}
