
package com.csii.pe.gateway.filter.ratelimit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import com.alibaba.fastjson.JSON;
import com.csii.pe.gateway.model.LimitConfig;
import com.csii.pe.gateway.model.LimitKey;

/**
 * See https://stripe.com/blog/rate-limiters and
 * https://gist.github.com/ptarjan/e38f45f2dfe601419ca3af937fff574d#file-1-check_request_rate_limiter-rb-L11-L34.
 *
 * @author Spencer Gibb
 * @author Ronny Bräunlich
 */
@ConfigurationProperties("spring.cloud.gateway.redis-rate-limiter")
public class CustomRedisRateLimiter extends AbstractRateLimiter<CustomRedisRateLimiter.Config>
		implements ApplicationContextAware {

	/**
	 * @deprecated use {@link Config#replenishRate}
	 */
	@Deprecated
	public static final String REPLENISH_RATE_KEY = "replenishRate";

	/**
	 * @deprecated use {@link Config#burstCapacity}
	 */
	@Deprecated
	public static final String BURST_CAPACITY_KEY = "burstCapacity";

	/**
	 * Redis Rate Limiter property name.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME = "redis-rate-limiter";

	/**
	 * Redis Script name.
	 */
	public static final String REDIS_SCRIPT_NAME = "redisRequestRateLimiterScript";

	/**
	 * Remaining Rate Limit header name.
	 */
	public static final String REMAINING_HEADER = "X-RateLimit-Remaining";

	/**
	 * Replenish Rate Limit header name.
	 */
	public static final String REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate";

	/**
	 * Burst Capacity Header name.
	 */
	public static final String BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity";

	private Log log = LogFactory.getLog(getClass());

	private ReactiveRedisTemplate<String, String> redisTemplate;

	private RedisScript<List<Long>> script;

	private AtomicBoolean initialized = new AtomicBoolean(false);

	private Config defaultConfig;

	// configuration properties
	/**
	 * Whether or not to include headers containing rate limiter information, defaults to
	 * true.
	 */
	private boolean includeHeaders = true;

	/**
	 * The name of the header that returns number of remaining requests during the current
	 * second.
	 */
	private String remainingHeader = REMAINING_HEADER;

	/** The name of the header that returns the replenish rate configuration. */
	private String replenishRateHeader = REPLENISH_RATE_HEADER;

	/** The name of the header that returns the burst capacity configuration. */
	private String burstCapacityHeader = BURST_CAPACITY_HEADER;

	public CustomRedisRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
			RedisScript<List<Long>> script, Validator validator) {
		super(Config.class, CONFIGURATION_PROPERTY_NAME, validator);
		this.redisTemplate = redisTemplate;
		this.script = script;
		initialized.compareAndSet(false, true);
	}

	public CustomRedisRateLimiter(int defaultReplenishRate, int defaultBurstCapacity) {
		super(Config.class, CONFIGURATION_PROPERTY_NAME, null);
		this.defaultConfig = new Config().setReplenishRate(defaultReplenishRate)
				.setBurstCapacity(defaultBurstCapacity);
	}

	static List<String> getKeys(String id) {
		// use `{}` around keys to use Redis Key hash tags
		// this allows for using redis cluster

		// Make a unique key per user.
		String prefix = "request_rate_limiter.{" + id;

		// You need two Redis keys for Token Bucket.
		String tokenKey = prefix + "}.tokens";
		String timestampKey = prefix + "}.timestamp";
		return Arrays.asList(tokenKey, timestampKey);
	}

	public boolean isIncludeHeaders() {
		return includeHeaders;
	}

	public void setIncludeHeaders(boolean includeHeaders) {
		this.includeHeaders = includeHeaders;
	}

	public String getRemainingHeader() {
		return remainingHeader;
	}

	public void setRemainingHeader(String remainingHeader) {
		this.remainingHeader = remainingHeader;
	}

	public String getReplenishRateHeader() {
		return replenishRateHeader;
	}

	public void setReplenishRateHeader(String replenishRateHeader) {
		this.replenishRateHeader = replenishRateHeader;
	}

	public String getBurstCapacityHeader() {
		return burstCapacityHeader;
	}

	public void setBurstCapacityHeader(String burstCapacityHeader) {
		this.burstCapacityHeader = burstCapacityHeader;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		if (initialized.compareAndSet(false, true)) {
			this.redisTemplate = context.getBean("stringReactiveRedisTemplate",
					ReactiveRedisTemplate.class);
			this.script = context.getBean(REDIS_SCRIPT_NAME, RedisScript.class);
			if (context.getBeanNamesForType(Validator.class).length > 0) {
				this.setValidator(context.getBean(Validator.class));
			}
		}
	}

	/* for testing */ Config getDefaultConfig() {
		return defaultConfig;
	}

	/**
	 * This uses a basic token bucket algorithm and relies on the fact that Redis scripts
	 * execute atomically. No other operations can run between fetching the count and
	 * writing the new count.
	 */
	@Override
	@SuppressWarnings("unchecked")
	/**
     * This uses a basic token bucket algorithm and relies on the fact that Redis scripts
     * execute atomically. No other operations can run between fetching the count and
     * writing the new count.
     */
    public Mono<Response> isAllowed(String routeId, String id) {
        if (!this.initialized.get()) {
            throw new IllegalStateException("RedisRateLimiter is not initialized");
        }

        LimitConfig limitConfig = getLimitConfig(routeId);
        
        if (limitConfig == null || limitConfig.getRateLimitConfig().size()==0) {
            return Mono.just(new Response(true,null));
        }

        Map<String, Config> conf = limitConfig.getRateLimitConfig();
        
        LimitKey limitKey = JSON.parseObject(id, LimitKey.class);
        //api限流
        String api = limitKey.getApiCode();
        Config apiConf = conf.get(api);
        if(apiConf==null){
        	apiConf = new Config();
        	apiConf.setBurstCapacity(20);
        	apiConf.setReplenishRate(1);
        }
        //渠道方限流
        String biz = limitKey.getChannelId();
        Config bizConf = conf.get(biz);
        final Config tempBizConf = bizConf;
        return isSingleAllow(api,routeId,apiConf).flatMap(res -> {
		    if (res.isAllowed()) {
		        if(tempBizConf!=null) {
		            return isSingleAllow(biz, routeId, tempBizConf);
		        }else {
		            return Mono.just(new Response(true,new HashMap<>()));
		        }
		    }else {
		        return Mono.just(res);
		    }
		} );
    }

    /**
     * 单级限流
     * @param api
     * @param routeId
     * @param apiConf
     * @return 
     */
    private Mono<Response> isSingleAllow(String key, String routeId, Config config) {
        // How many requests per second do you want a user to be allowed to do?
        int replenishRate = config.getReplenishRate();
        // How much bursting do you want to allow?
        int burstCapacity = config.getBurstCapacity();
        try {
            List<String> keys = getKeys(routeId+"$"+key);
            // The arguments to the LUA script. time() returns unixtime in seconds.
            List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "",
                    Instant.now().getEpochSecond() + "", "1");
            Flux<List<Long>> flux = this.redisTemplate.execute(this.script, keys, scriptArgs);
            return flux.onErrorResume(throwable -> Flux.just(Arrays.asList(1L, -1L)))
                    .reduce(new ArrayList<Long>(), (longs, l) -> {
                        longs.addAll(l);
                        return longs;
                    }) .map(results -> {
                        boolean allowed = results.get(0) == 1L;
                        Long tokensLeft = results.get(1);

                        Response response = new Response(allowed, getHeaders(config, tokensLeft));

                        if (log.isDebugEnabled()) {
                            log.debug("response: " + response);
                        }
                        return response;
                    });
        }
        catch (Exception e) {
            /*
             * We don't want a hard dependency on Redis to allow traffic. Make sure to set
             * an alert so you know if this is happening too much. Stripe's observed
             * failure rate is 0.01%.
             */
            log.error("Error determining if user allowed from redis", e);
        }
        return Mono.just(new Response(true, getHeaders(config, -1L)));
    }

    private LimitConfig getLimitConfig(String routeId) {
        Map<String, LimitConfig> map = new HashMap<>();
        LimitConfig limitConfig = new LimitConfig();
        limitConfig.setRouteId("rateLimit_route");
        Map<String, Config> tokenMap = new HashMap<>();
        Config apiConfig = new Config();
        apiConfig.setBurstCapacity(1);
        apiConfig.setReplenishRate(1);
        
        Config bizConfig = new Config();
        bizConfig.setBurstCapacity(1);
        bizConfig.setReplenishRate(1);
        
        tokenMap.put("/hello/rateLimit", apiConfig);
//        tokenMap.put("jieyin", bizConfig);
        limitConfig.setRateLimitConfig(tokenMap);
        map.put("rateLimit_route", limitConfig);
        return limitConfig;
    }

	@NotNull
	public Map<String, String> getHeaders(Config config, Long tokensLeft) {
		Map<String, String> headers = new HashMap<>();
		if (isIncludeHeaders()) {
			headers.put(this.remainingHeader, tokensLeft.toString());
			headers.put(this.replenishRateHeader,
					String.valueOf(config.getReplenishRate()));
			headers.put(this.burstCapacityHeader,
					String.valueOf(config.getBurstCapacity()));
		}
		return headers;
	}

	@Validated
	public static class Config {

		@Min(1)
		private int replenishRate;

		@Min(1)
		private int burstCapacity = 1;

		public int getReplenishRate() {
			return replenishRate;
		}

		public Config setReplenishRate(int replenishRate) {
			this.replenishRate = replenishRate;
			return this;
		}

		public int getBurstCapacity() {
			return burstCapacity;
		}

		public Config setBurstCapacity(int burstCapacity) {
			this.burstCapacity = burstCapacity;
			return this;
		}

		@Override
		public String toString() {
			return "Config{" + "replenishRate=" + replenishRate + ", burstCapacity="
					+ burstCapacity + '}';
		}

	}

}
