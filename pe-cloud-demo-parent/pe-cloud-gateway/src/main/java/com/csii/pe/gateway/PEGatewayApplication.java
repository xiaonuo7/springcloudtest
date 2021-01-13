package com.csii.pe.gateway;

import javax.servlet.Servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;


@SpringBootApplication
@EnableDiscoveryClient
@Import({ServiceConfigucation.class})
@ImportResource({"classpath:/META-INF/config/*.xml","classpath:/META-INF/gateway/*.xml"})
public class PEGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PEGatewayApplication.class,args);
    }
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ServletRegistrationBean getMainServlet(Servlet mainServlet) {
		ServletRegistrationBean registrationBean = new ServletRegistrationBean(mainServlet);
		registrationBean.setLoadOnStartup(0);// level 0
		registrationBean.addUrlMappings("/gateway/*"); // urlPattern
		// 指定Name，如不指定,默认为DispatcherServlet
		registrationBean.setName("gateway");
		return registrationBean;
	}

}
