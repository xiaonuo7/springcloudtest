package com.csii.pe.gateway.filter.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSON;
import com.csii.pe.gateway.model.LimitKey;

import reactor.core.publisher.Mono;

public class CustomKeyResolver implements KeyResolver {

    public static final String BEAN_NAME = "customKeyResolver";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(getKey(exchange));
    }

    /**
     * 
     * @param exchange
     * @return
     */
    private String getKey(ServerWebExchange exchange) {
        
        LimitKey limitKey = new LimitKey();
        
        limitKey.setApiCode(exchange.getRequest().getPath().toString());
        limitKey.setChannelId(exchange.getRequest().getQueryParams().getFirst("channelId"));

        return JSON.toJSONString(limitKey);
    }
}