package collectorMain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        R2dbcAutoConfiguration.class,
        R2dbcDataAutoConfiguration.class
})
@ComponentScan(basePackages = {"collectorMain", "controller", "service", "dto", "kafkaConfig"})
public class CollectorMain {

    public static void main(String[] args) {
        SpringApplication.run(CollectorMain.class, args);
    }
}