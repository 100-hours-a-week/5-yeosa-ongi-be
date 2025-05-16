package ongi.ongibe.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("[Async 예외] 메서드: {}, 메시지: {}", method.getName(), ex.getMessage(), ex);
        };
    }

    @Bean(name = "asyncExecutor")
    public TaskExecutor asyncExecutor() {
        return new ConcurrentTaskExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }

}
