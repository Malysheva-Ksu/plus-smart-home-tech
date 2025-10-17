package main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import service.AggregationStarter;

@SpringBootApplication
@Slf4j
@ComponentScan(basePackages = {"service", "kafka"})
public class MainAggregator {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainAggregator.class, args);

        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();

        context.close();
        log.info("Сервис Aggregator завершил свою работу.");
    }
}