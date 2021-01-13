package com.csii.pe.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class ConfigFields {
    @Value("${config.test}")
    private String configTest;
    public String getConfigTest() {
        return configTest;
    }

}
