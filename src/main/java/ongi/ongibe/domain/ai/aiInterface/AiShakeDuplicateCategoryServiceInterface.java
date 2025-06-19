package ongi.ongibe.domain.ai.aiInterface;

import java.util.List;

public interface AiShakeDuplicateCategoryServiceInterface {

    void analyzeShakyDuplicateCategory(Long albumId, Long userId, List<String> s3keys);

}
