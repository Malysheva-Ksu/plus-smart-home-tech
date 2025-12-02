package main;

import model.warehouse.ReserveRequest;
import model.warehouse.StockItem;
import model.warehouse.StockMovement;
import model.warehouse.StockUpdateRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"controller", "model", "repository", "service", "config"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "model.warehouse")
public class Warehouse {
    public static void main(String[] args) {
        SpringApplication.run(Warehouse.class, args);
    }
}