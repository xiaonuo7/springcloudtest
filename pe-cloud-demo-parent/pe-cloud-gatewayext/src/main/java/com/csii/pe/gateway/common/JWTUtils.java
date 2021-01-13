package com.csii.pe.gateway.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.fastjson.JSON;
import com.sun.jersey.core.util.Base64;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@ConfigurationProperties("jwt.config")
public class JWTUtils {
	 String key = "123456";
    /**
     * 由字符串生成加密key
     *
     * @return
     */
    public SecretKey generalKey() {
        // 本地的密码解码
        byte[] encodedKey = key.getBytes();
        // 根据给定的字节数组使用AES加密算法构造一个密钥
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }
 
    /**
     * 创建jwt
     * @param id
     * @param issuer
     * @param subject
     * @param ttlMillis
     * @return
     * @throws Exception
     */
    public String createJWT(Map<String, Object> claims,long ttlMillis) throws Exception {
    	Map<String, Object> header = new HashMap();
	    header.put("alg", "HS256");
        // 指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
 
        // 生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
 
        // 创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("uid", "123456");
//        claims.put("user_name", "admin");
//        claims.put("nick_name", "X-rapido");
 
        // 生成签名的时候使用的秘钥secret，切记这个秘钥不能外露哦。它就是你服务端的私钥，在任何场景都不应该流露出去。
        // 一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
        SecretKey key = generalKey();
 
        // 下面就是在为payload添加各种标准声明和私有声明了
        JwtBuilder builder = Jwts.builder() // 这里其实就是new一个JwtBuilder，设置jwt的body
                .setHeader(header).setClaims(claims).signWith(signatureAlgorithm, key); // 设置签名使用的签名算法和签名使用的秘钥
 
        // 设置过期时间
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }
 
    /**
     * 解密jwt
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public Claims parseJWT(String jwt) throws Exception {
        SecretKey key = generalKey();  //签名秘钥，和生成的签名的秘钥一模一样
        Claims claims = Jwts.parser()  //得到DefaultJwtParser
                .setSigningKey(key)                 //设置签名的秘钥
                .parseClaimsJws(jwt).getBody();     //设置需要解析的jwt
        return claims;
    }
    
    public static void main(String[] args) {
    	 
        try {
		    Map payload = new HashMap();
		    payload.put("userName", "1111");
            JWTUtils util = new JWTUtils();
            String jwt = util.createJWT(payload,6000L);
            System.out.println("JWT：" + jwt);
 
            System.out.println("\n解密\n");
            Claims c = util.parseJWT(jwt);
            System.out.println(c.get("userName"));
            System.out.println(new String(Base64.decode("eyJhbGciOiJIUzI1NiJ9".getBytes())));
            Claims c1 = util.parseJWT(jwt);
            System.out.println(c.getIssuedAt());
            System.out.println(c.getSubject());
            System.out.println(c.getIssuer());
            System.out.println(c.get("uid", String.class));
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
}
