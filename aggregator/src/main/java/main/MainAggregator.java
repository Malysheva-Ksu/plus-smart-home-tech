package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import service.AggregationStarter;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"service", "kafka", "main"})
public class MainAggregator {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainAggregator.class, args);

        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();

        context.close();
    }
}