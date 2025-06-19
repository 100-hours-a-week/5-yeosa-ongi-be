package ongi.ongibe.domain.ai.aiInterface;

import java.util.List;

public interface AiEmbeddingServiceInterface {

    void requestEmbeddings(Long albumId, Long userId, List<String> s3keys);
}
