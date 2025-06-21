package ongi.ongibe.domain.ai.consumer;

public interface AiConsumerInterface<T> {
    void consume(T response);
}
