package ongi.ongibe.testKafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka")
public class KafkaTestController {

    private final SimpleKafkaProducer producer;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String msg) {
        producer.send("test-topic", msg);
        return "Sent: " + msg;
    }
}
