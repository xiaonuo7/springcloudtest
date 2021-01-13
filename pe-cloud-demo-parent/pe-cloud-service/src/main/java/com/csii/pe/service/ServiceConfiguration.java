package com.csii.pe.service;

import com.csii.pe.channel.http.servlet.MainServlet0;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;

@Configuration
public class ServiceConfiguration {
    @Bean
    public Servlet mainServlet() {
        MainServlet0 servlet = new MainServlet0();
        return servlet;
    }

    @Bean
    public ServletRegistrationBean mainServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(mainServlet());
        registration.getUrlMappings().clear();
        registration.addUrlMappings("*.do");
        return registration;
    }
}
