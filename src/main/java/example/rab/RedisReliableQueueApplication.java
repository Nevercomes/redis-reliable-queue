package example.rab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"example.rab"})
public class RedisReliableQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisReliableQueueApplication.class, args);
    }

}
