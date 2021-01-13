package com.csii.pe.gateway;

import com.csii.pe.service.comm.CommunicationException;
import com.csii.pe.service.comm.Transport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class RestTransport implements Transport {
    @Autowired
    RestTemplate restTemplate;

    private String trsCodeName = "_TransactionId";
    private String serviceName = "_ServiceName";

    public Object submit(Object data) throws CommunicationException {
        Map request = (Map) data;
        String trsId = getValue(request,trsCodeName,"TrsId is null");
        String service = getValue(request,serviceName,"Service is null");
        return restTemplate.postForObject("http://" + service + "/" + trsId + ".do",data,Map.class);
    }



    private String getValue(Map request,String key,String errorMsg) throws CommunicationException {
        String trsId = (String) request.get(key);
        if(trsId == null){
            throw new CommunicationException(errorMsg);
        }
        request.remove(key);
        return trsId;
    }

    public void setTrsCodeName(String trsCodeName){
        this.trsCodeName = trsCodeName;
    }
    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
}