package ongi.ongibe.global.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class KafkaListenerFactoryHelper {

    public static <T> ConcurrentKafkaListenerContainerFactory<String, T> buildListenerFactory(
            KafkaProperties kafkaProperties,
            TypeReference<T> typeReference,
            DefaultErrorHandler errorHandler,
            int concurrency,
            boolean isBatch
    ) {
        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(typeReference);
        valueDeserializer.addTrustedPackages("*");

        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(valueDeserializer)
        );

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.setBatchListener(isBatch);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
