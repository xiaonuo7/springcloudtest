package com.csii.pe.gateway.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

import com.alibaba.fastjson.JSON;
import com.csii.pe.gateway.common.CommCanstants;
import com.csii.pe.gateway.mapper.UserMapper;
import com.csii.pe.gateway.model.LoginUser;
import com.csii.pe.gateway.model.RespResult;

import reactor.core.publisher.Flux;

@RestController
public class PermissionCheckController {
	@Autowired
	LoginController loginController;
	List<String> needLoginList;
	@Autowired
	UserMapper userMapper;
	protected Log log = LogFactory.getLog(this.getClass());
	public PermissionCheckController(){
		needLoginList = new ArrayList<String>();
		needLoginList.add("queryProduct");
	}
	public boolean permissionCheck(String requestId) {
		if(needLoginList.contains(requestId)){
			return true;
		}
		return false;
	}
}
