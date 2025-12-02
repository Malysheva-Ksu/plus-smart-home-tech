package config;

import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EurekaConfig {

    @Bean
    @Primary
    public EurekaClientConfigBean eurekaClientConfigBean() {
        EurekaClientConfigBean config = new EurekaClientConfigBean();
        config.setRegisterWithEureka(true);
        config.setFetchRegistry(true);
        return config;
    }
}