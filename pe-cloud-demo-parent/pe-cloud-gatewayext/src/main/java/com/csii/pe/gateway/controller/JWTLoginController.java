package com.csii.pe.gateway.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

import com.csii.pe.gateway.common.JWTUtils;
import com.csii.pe.gateway.mapper.UserMapper;
import com.csii.pe.gateway.model.LoginUser;
import com.csii.pe.gateway.redis.RedisUtil;

import io.jsonwebtoken.Claims;

@RestController
public class JWTLoginController {
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	UserMapper userMapper;
	protected Log log = LogFactory.getLog(this.getClass());
	
	public boolean loginCheck(String token) {
		log.info("loginCheck token ...." + token);
		if (token!=null&&redisUtil.contains(token)) {
			log.info("loginCheck token exist...." + token);
			return true;
		} else {
			return false;
		}
	}
	public String getToken(String userName){
		return (String)redisUtil.get(userName);
	}
	public String doLogin(String userName,String password){
		log.info("doLogin token ....");
		List<LoginUser> loginUser = userMapper.queryUser(userName, password);
		if(loginUser==null||loginUser.size()==0){
            return null;
		}else{
			String userId = loginUser.get(0).getUserId();
		    JWTUtils util = new JWTUtils();
		    Map claims = new HashMap();
		    claims.put("userName", userName);
            String jwtToken=null;
			try {
				jwtToken = util.createJWT(claims,60000L);
			} catch (Exception e) {
				return null;
			}
            System.out.println("jwtToken：" + jwtToken);
			if(redisUtil.contains(userId)){
				//登录成功时需要同时移除当前session下（有可能当前session已经存在另一个用户2的会话）和此次登录的用户1之前的会话
//				String currentSession = "spring:session:sessions:"+sessionRedisKey;
//				LoginUser currentSessionUser = (LoginUser) data.get(sessionRedisKey);
//				if(currentSessionUser!=null){
//					redisUtil.remove(currentSessionUser.getUserId());//移除当前会话
//					redisUtil.remove(currentSession);//移除当前会话
//				}
				if(redisUtil.contains(userId)){
					redisUtil.remove((String)redisUtil.get(userId));//移除被登录用户的会话
					redisUtil.remove(userId);
				}
			}
			redisUtil.set(jwtToken, loginUser.get(0),160L);
			redisUtil.set(userId, jwtToken,160L);
	        return userId;
		}
	}
}
