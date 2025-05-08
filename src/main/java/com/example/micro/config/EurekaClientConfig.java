package com.example.micro.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
public class EurekaClientConfig {
    // The @EnableDiscoveryClient annotation is enough to register with Eureka
}