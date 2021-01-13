package com.csii.pe.gateway.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class ConfigFields {
	@Value("${gray.flag}")
	private boolean grayFlag;
    public boolean getGrayFlag() {
        return grayFlag;
    }
	@Value("${gray.strategy}")
	private String grayStrategy;
    public String getGrayStrategy() {
        return grayStrategy;
    }
	@Value("${gray.value}")
	private String grayValue;
    public String getGrayValue() {
        return grayValue;
    }
	@Value("${gray.version}")
	private String grayVersion;
    public String getGrayVersion() {
        return grayVersion;
    }
}
