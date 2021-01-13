package com.csii.pe.gateway;

import javax.servlet.Servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;


@SpringBootApplication
@EnableDiscoveryClient
@ImportResource({"classpath:/META-INF/config/*.xml"})
public class SCGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SCGatewayApplication.class,args);
    }
}
