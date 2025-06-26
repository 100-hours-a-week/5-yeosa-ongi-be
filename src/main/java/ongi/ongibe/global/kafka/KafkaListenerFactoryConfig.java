package ongi.ongibe.global.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO;
import ongi.ongibe.domain.ai.dto.AiEmbeddingResponseDTO;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.dto.DuplicateResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.dto.ShakyResponseDTO;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaListenerFactoryConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaResponseDTOWrapper<ShakyResponseDTO>> shakeKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        return KafkaListenerFactoryHelper.buildListenerFactory(
                kafkaProperties,
                new TypeReference<>() {},
                errorHandler,
                2,
                true
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaResponseDTOWrapper<DuplicateResponseDTO>> duplicateKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        return KafkaListenerFactoryHelper.buildListenerFactory(
                kafkaProperties,
                new TypeReference<>() {},
                errorHandler,
                2,
                true
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaResponseDTOWrapper<AiClusterResponseDTO>> clusterKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        return KafkaListenerFactoryHelper.buildListenerFactory(
                kafkaProperties,
                new TypeReference<>() {},
                errorHandler,
                2,
                true
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaResponseDTOWrapper<CategoryResponseDTO>> categoryKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        return KafkaListenerFactoryHelper.buildListenerFactory(
                kafkaProperties,
                new TypeReference<>() {},
                errorHandler,
                2,
                true
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>> aestheticKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        return KafkaListenerFactoryHelper.buildListenerFactory(
                kafkaProperties,
                new TypeReference<>() {},
                errorHandler,
                2,
                true
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaResponseDTOWrapper<AiEmbeddingResponseDTO>> embeddingKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        return KafkaListenerFactoryHelper.buildListenerFactory(
                kafkaProperties,
                new TypeReference<>() {},
                errorHandler,
                2,
                true
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dlqKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(new DefaultErrorHandler());
        return factory;
    }


    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        FixedBackOff fixedBackOff = new FixedBackOff(3000L, 2);
        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
    }

}
