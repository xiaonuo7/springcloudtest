package com.csii.pe.gateway.filter.system;

import java.nio.charset.Charset;

//import org.gateway.response.Response;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.csii.pe.gateway.common.CommCanstants;
import com.csii.pe.gateway.model.RespResult;

//import com.alibaba.fastjson.JSON;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class WrapperResponseFilter implements GlobalFilter, Ordered {
	String encoding ="UTF-8";
	protected Log logger = LogFactory.getLog(this.getClass());
	private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    @Override
    public int getOrder() {
        // -1 is response write filter, must be called before that
        return -2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //记录请求开始时间
        exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        originalResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                        // probably should reuse buffers
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        // 释放掉内存
                        DataBufferUtils.release(dataBuffer);
                        String rs = new String(content, Charset.forName(encoding));
                        RespResult response = new RespResult(CommCanstants.RC_Success,"请求成功",rs);
                        logger.info(exchange.getRequest().getURI()+"-"+rs);
                        //有时候GBK才能正常解析中文，有时候UTF-8才行，待处理，wangshuai
                        byte[] newRs = JSON.toJSONString(response).getBytes(Charset.forName(encoding));
                        originalResponse.getHeaders().setContentLength(newRs.length);//如果不重新设置长度则收不到消息。

                        Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                        if (startTime != null) {
                            //打印
                        	logger.info(exchange.getRequest().getURI() + " 耗时" + (System.currentTimeMillis() - startTime));
                        }   
                        return bufferFactory.wrap(newRs);
                    }));
                }
                // if body is not a flux. never got there.
                return super.writeWith(body);
            }
        };
        // replace response with decorator
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}