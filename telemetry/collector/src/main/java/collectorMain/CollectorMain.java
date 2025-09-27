package collectorMain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        R2dbcAutoConfiguration.class,
        R2dbcDataAutoConfiguration.class 
})
public class CollectorMain {

    public static void main(String[] args) {
        SpringApplication.run(CollectorMain.class, args);
    }
}