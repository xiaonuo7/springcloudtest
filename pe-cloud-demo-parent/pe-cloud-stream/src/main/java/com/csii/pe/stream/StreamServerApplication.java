package com.csii.pe.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StreamServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StreamServerApplication.class,args);
    }
}
