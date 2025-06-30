package ongi.ongibe.domain.ai.repository.queryDSL;

public interface AiTaskStatusRepositoryQueryDSL {
    int countSuccessStepsByAlbumId(Long albumId);
}
