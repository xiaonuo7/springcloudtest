package com.csii.pe.gateway;

import com.csii.pe.channel.http.servlet.MainServlet0;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Servlet;

@Configuration
public class ServiceConfigucation {

    @Bean
    public Servlet mainServlet() {
    	MainServletExt servlet = new MainServletExt();
        return servlet;
    }

    @Bean
    public ServletRegistrationBean mainServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(mainServlet());
        registration.getUrlMappings().clear();
        registration.addUrlMappings("*.do");
        return registration;
    }

    @Bean
    @LoadBalanced
    RestTemplate restTemplate(){return new RestTemplate();}

    @Bean
    RestTransport restTransport(){return new RestTransport();}

}
