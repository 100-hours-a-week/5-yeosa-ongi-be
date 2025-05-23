package ongi.ongibe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OngiBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(OngiBeApplication.class, args);
    }

}