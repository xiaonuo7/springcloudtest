package com.csii.pe.gateway.model;

import java.util.Map;

import com.csii.pe.gateway.filter.ratelimit.CustomRedisRateLimiter;
import com.csii.pe.gateway.filter.ratelimit.CustomRedisRateLimiter.Config;


public class LimitConfig {
	private Map<String, Config> rateLimitConfig;
	private String routeId;

	public Map<String, Config> getRateLimitConfig() {
		return rateLimitConfig;
	}

	public void setRateLimitConfig(Map<String, Config> rateLimitConfig) {
		this.rateLimitConfig = rateLimitConfig;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
}
