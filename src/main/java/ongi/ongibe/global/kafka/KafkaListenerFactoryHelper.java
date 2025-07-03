package ongi.ongibe.global.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
        valueDeserializer.ignoreTypeHeaders();
        valueDeserializer.setRemoveTypeHeaders(false);
        valueDeserializer.setUseTypeMapperForKey(false); // 메시지 key가 JSON이면 true
        valueDeserializer.setUseTypeHeaders(false); // type headers 사용 X

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
