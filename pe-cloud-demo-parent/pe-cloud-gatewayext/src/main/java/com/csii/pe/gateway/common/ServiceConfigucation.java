package com.csii.pe.gateway.common;

import com.csii.pe.gateway.filter.loadbalance.UserIdLoadBalancerClientFilter;
import com.csii.pe.gateway.filter.ratelimit.CustomKeyResolver;
import com.csii.pe.gateway.filter.ratelimit.CustomRedisRateLimiter;
import com.csii.pe.gateway.filter.ratelimit.RateLimiterGatewayFilterFactory;
import com.csii.pe.gateway.redis.RedisConfig;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
//import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.validation.Validator;
import java.lang.reflect.Method;
import java.util.List;

@Configuration
@EnableCaching
public class ServiceConfigucation extends CachingConfigurerSupport{
//    @Bean
//    public KeyResolver userKeyResolver() {
//        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("user"));
//    }

	@Autowired
	private RedisConfig redisConn;

	/**
	 * 生产key的策略
	 *
	 * @return
	 */

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {

			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};

	}

	/**
	 * 管理缓存
	 *
	 * @param redisTemplate
	 * @return
	 */

	@SuppressWarnings("rawtypes")
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		RedisCacheManager cacheManager = RedisCacheManager.create(factory);
		return cacheManager;
	}

	/**
	 * redis 数据库连接池
	 * 
	 * @return
	 */

//	@SuppressWarnings("deprecation")
//	@Bean
//	public JedisConnectionFactory redisConnectionFactory() {
//		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
//		redisStandaloneConfiguration.setHostName(redisConn.getHost());
//		redisStandaloneConfiguration.setPort(redisConn.getPort());
//		return new JedisConnectionFactory(redisStandaloneConfiguration);
//	}
	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		return new LettuceConnectionFactory(redisConn.getHost(),redisConn.getPort());
	}
	
	/**
	 * redisTemplate配置
	 *
	 * @param factory
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory factory) {
		StringRedisTemplate template = new StringRedisTemplate(factory);
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setValueSerializer(jackson2JsonRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}
	
    @Bean
    @Primary
    public CustomKeyResolver customKeyResolver(){
    	return new CustomKeyResolver();
    }

//    @Bean
//    @Primary
//    public LettuceConnectionFactory reactiveRedisConnectionFactory() {
//        return new LettuceConnectionFactory();
//	}
//    @Bean
//    @Primary
//    public ReactiveRedisTemplate<?, ?> reactiveRedisTemplate(LettuceConnectionFactory connectionFactory,
//			RedisSerializationContext<?, ?> serializationContext) {
//        return new ReactiveRedisTemplate(connectionFactory, serializationContext);
//	}
    @Bean
    @Primary
    public CustomRedisRateLimiter customRedisRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
                                             @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
                                             Validator validator) {
        return new CustomRedisRateLimiter(redisTemplate, redisScript, validator);
    }
    @Bean
    public RateLimiterGatewayFilterFactory rateLimiterGatewayFilterFactory(CustomRedisRateLimiter customRedisRateLimiter, CustomKeyResolver customKeyResolver) {
        return new RateLimiterGatewayFilterFactory(customRedisRateLimiter, customKeyResolver);
    }  
    @Bean
    public UserIdLoadBalancerClientFilter userLoadBalanceClientFilter(LoadBalancerClient client, LoadBalancerProperties properties) {
        return new UserIdLoadBalancerClientFilter(client, properties);
    }
}
