package ongi.ongibe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableKafka
@EnableRetry
@SpringBootApplication
public class OngiBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(OngiBeApplication.class, args);
    }

}