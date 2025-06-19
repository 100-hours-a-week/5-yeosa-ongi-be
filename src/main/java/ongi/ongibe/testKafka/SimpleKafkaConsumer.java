package ongi.ongibe.testKafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SimpleKafkaConsumer {

    @KafkaListener(topics = "test-topic")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            System.out.println("[CONSUMER] Received: " + record.value());
        } catch (Exception e) {
            System.err.println("❗ Kafka consumer exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
