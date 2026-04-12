package io.agh.iot.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.agh.iot.gateway.config.GatewayRoutesProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayRoutesProperties.class)
@ConfigurationPropertiesScan
public class GatewayApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApiApplication.class, args);
    }
}
