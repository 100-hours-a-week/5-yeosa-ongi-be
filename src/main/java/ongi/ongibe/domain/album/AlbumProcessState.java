package ongi.ongibe.domain.album;

public enum AlbumProcessState {
    NOT_STARTED,   // 시작 전 (앨범 생성 직후)
    IN_PROGRESS,   // AI 처리 중
    DONE,          // 성공적으로 완료됨
    FAILED         // 실패함 (재시도 가능)
}