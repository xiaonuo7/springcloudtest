package com.csii.pe.gateway.filter.system;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

//import org.gateway.response.Response;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.csii.pe.gateway.common.CommCanstants;
import com.csii.pe.gateway.controller.JWTLoginController;
import com.csii.pe.gateway.controller.LoginController;
import com.csii.pe.gateway.controller.PermissionCheckController;
import com.csii.pe.gateway.model.LoginUser;
import com.csii.pe.gateway.model.RespResult;

//import com.alibaba.fastjson.JSON;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GlobalBussControlFilter implements GlobalFilter, Ordered {
	String encoding ="UTF-8";
	protected Log log = LogFactory.getLog(this.getClass());
	@Autowired
	JWTLoginController loginController;
	@Autowired
	PermissionCheckController permissionCheckController;
    @Override
    public int getOrder() {
        // -1 is response write filter, must be called before that
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userName = exchange.getRequest().getQueryParams().getFirst("userName");
        String password = exchange.getRequest().getQueryParams().getFirst("password");
        String token = exchange.getRequest().getHeaders().getFirst("token");
        String[] paths = exchange.getRequest().getURI().getPath().replace(".do", "").split("/");
		final ServerHttpResponse response = exchange.getResponse();
        String requestId = paths[paths.length-1];
    		if("login".equals(requestId)){
    			String uniqId = loginController.doLogin(userName, password);
    			if(uniqId!=null){
    				exchange.getResponse().getHeaders().add("token", loginController.getToken(uniqId));
                    return chain.filter(exchange);
    			}else{
    	            String str = "{\"_RejCode\":\"99999\",\"_RejMsg\":\"用户名或密码不正确\"}";
    	            byte[] bytes = str.getBytes(Charset.forName(encoding));
    	            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    	            response.setStatusCode(HttpStatus.OK);
    	            log.info("login failed............");
    	            return response.writeWith(Flux.just(buffer));//设置body
    			}
    		}else{
    			if(permissionCheckController.permissionCheck(requestId)){
    				if(!loginController.loginCheck(token)){//未登录
        	            byte[] bytes = "{\"_RejCode\":\"99999\",\"_RejMsg\":\"请先登录\"}".getBytes(Charset.forName("UTF-8"));
        	            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        	            response.setStatusCode(HttpStatus.OK);
        	            return response.writeWith(Flux.just(buffer));//设置body
    				}
    			}
                return chain.filter(exchange);
    		}
    }
}