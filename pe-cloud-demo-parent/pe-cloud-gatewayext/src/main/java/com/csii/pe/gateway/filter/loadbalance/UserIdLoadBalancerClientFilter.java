package com.csii.pe.gateway.filter.loadbalance;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.web.server.ServerWebExchange;

@RefreshScope
public class UserIdLoadBalancerClientFilter extends LoadBalancerClientFilter {
    public UserIdLoadBalancerClientFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }
 
    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        //这里可以拿到web请求的上下文，可以从header中取出来自己定义的数据。
        String userId = exchange.getRequest().getQueryParams().getFirst("userId");
         if (userId == null) {
             return super.choose(exchange);
         }
        if (this.loadBalancer instanceof RibbonLoadBalancerClient) {
            RibbonLoadBalancerClient client = (RibbonLoadBalancerClient) this.loadBalancer;
            String serviceId = ((URI) exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR)).getHost();
            //这里使用userId做为选择服务实例的key
            return client.choose(serviceId, userId);
        }
        return super.choose(exchange);
    }
 
}