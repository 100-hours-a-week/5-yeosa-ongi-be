package ongi.ongibe.cache.album;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlbumCacheServiceTest {

    @InjectMocks
    private AlbumCacheService albumCacheService;

    @Mock
    private UserAlbumRepository userAlbumRepository;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private RedisCacheService redisCacheService;

    private static final Duration TTL = Duration.ofSeconds(10);
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);

    private final Long userId = 1L;
    private final String yearMonth = "2025-07";

    private final String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
    private final String lockKey = key + ":lock";

    @Test
    void testReturnFromCache() {
        // given
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        MonthlyAlbumResponseDTO expected = mock(MonthlyAlbumResponseDTO.class);
        given(redisCacheService.get(key, MonthlyAlbumResponseDTO.class)).willReturn(Optional.of(expected));

        // when
        MonthlyAlbumResponseDTO result = albumCacheService.getMonthlyAlbum(userId, yearMonth);

        // then
        assertThat(result).isEqualTo(expected);
        verifyNoInteractions(securityUtil, userAlbumRepository); // DB 접근 없음
    }

    @Test
    void testLockAcquiredAndBuildFromDB() {
        // given
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        String lockKey = key + ":lock";

        given(redisCacheService.get(key, MonthlyAlbumResponseDTO.class)).willReturn(Optional.empty());
        given(redisCacheService.tryLock(lockKey, LOCK_TTL)).willReturn(true);

        User user = mock(User.class);

        given(securityUtil.getCurrentUser()).willReturn(user);
        given(userAlbumRepository.findAllByUser(user)).willReturn(List.of()); // empty list
        given(userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(eq(user), any())).willReturn(false);

        // when
        MonthlyAlbumResponseDTO result = albumCacheService.getMonthlyAlbum(userId, yearMonth);

        // then
        assertThat(result).isNotNull();
        verify(redisCacheService).set(eq(key), any(), eq(TTL));
        verify(redisCacheService).unlock(lockKey);
    }

    @Test
    void testLockFailedThenRetrySuccess() {
        // given
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        String lockKey = key + ":lock";

        given(redisCacheService.get(key, MonthlyAlbumResponseDTO.class)).willReturn(Optional.empty()); // 1st try
        given(redisCacheService.tryLock(lockKey, LOCK_TTL)).willReturn(false);
        // retry 후 2nd try에 캐시 조회 성공
        given(redisCacheService.get(key, MonthlyAlbumResponseDTO.class))
                .willReturn(Optional.empty()) // 1st retry
                .willReturn(Optional.of(mock(MonthlyAlbumResponseDTO.class))); // 2nd retry

        // when
        MonthlyAlbumResponseDTO result = albumCacheService.getMonthlyAlbum(userId, yearMonth);

        // then
        assertThat(result).isNotNull();
        verify(securityUtil, never()).getCurrentUser(); // DB 접근 없음
    }

    @Test
    void testLockFailedAndAllRetriesFailThenThrow() {
        // given
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        String lockKey = key + ":lock";

        given(redisCacheService.get(key, MonthlyAlbumResponseDTO.class)).willReturn(Optional.empty());
        given(redisCacheService.tryLock(lockKey, LOCK_TTL)).willReturn(false);

        // retry 실패 5회
        given(redisCacheService.get(key, MonthlyAlbumResponseDTO.class)).willReturn(Optional.empty());

        // when&then
        assertThatThrownBy(() -> albumCacheService.getMonthlyAlbum(userId, yearMonth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("캐시 로딩 실패");

        verify(securityUtil, never()).getCurrentUser();
    }

    @Test
    void test_onlyOneThreadAcquiresLock_andBuildsCache() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        MonthlyAlbumResponseDTO mockResponse = new MonthlyAlbumResponseDTO(List.of(), null, false);

        // 캐시 첫 조회는 모두 miss
        when(redisCacheService.get(eq(key), eq(MonthlyAlbumResponseDTO.class)))
                .thenReturn(Optional.empty()) // 첫 캐시 miss
                .thenReturn(Optional.empty()) // 재시도
                .thenReturn(Optional.of(mockResponse)); // 다음부터는 캐시 hit

        // 락 획득 여부: 하나의 쓰레드만 true, 나머지는 false
        AtomicBoolean lockAcquired = new AtomicBoolean(false);
        when(redisCacheService.tryLock(eq(lockKey), any()))
                .thenAnswer(inv -> lockAcquired.compareAndSet(false, true));

        when(securityUtil.getCurrentUser()).thenReturn(new User());
        when(userAlbumRepository.findAllByUser(any())).thenReturn(List.of());
        when(userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(any(), any())).thenReturn(false);

        List<Future<MonthlyAlbumResponseDTO>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> {
                startLatch.await();
                try {
                    return albumCacheService.getMonthlyAlbum(userId, yearMonth);
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        // 모든 스레드 시작
        startLatch.countDown();
        endLatch.await();

        // 모든 응답은 동일 객체여야 함
        for (Future<MonthlyAlbumResponseDTO> result : results) {
            assertThat(result.get()).isEqualTo(mockResponse);
        }

        // DB 조회 및 캐시 저장은 단 한 번만 발생
        verify(userAlbumRepository, times(1)).findAllByUser(any());
        verify(redisCacheService, times(1)).set(eq(key), any(), any());
    }
}
