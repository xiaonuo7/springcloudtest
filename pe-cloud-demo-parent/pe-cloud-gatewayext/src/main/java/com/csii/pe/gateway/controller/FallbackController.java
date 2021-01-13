package com.csii.pe.gateway.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.csii.pe.gateway.common.CommCanstants;
import com.csii.pe.gateway.model.RespResult;
 
@RestController
@RequestMapping("/fallback")
public class FallbackController {
 
    @RequestMapping(value = {"/gatewayFallback"})
    public String gatewayFallback(){
        RespResult response = new RespResult(CommCanstants.RC_Fail,"请求超时，请稍后再试",null);
        return JSON.toJSONString(response);
    }
}
