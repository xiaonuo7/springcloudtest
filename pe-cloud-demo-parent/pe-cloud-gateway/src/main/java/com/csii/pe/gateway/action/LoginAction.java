package com.csii.pe.gateway.action;

import com.csii.pe.action.Action;
import com.csii.pe.action.Executable;
import com.csii.pe.core.Context;
import com.csii.pe.core.PeException;
import com.csii.pe.gateway.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class LoginAction  implements Action, Executable {

    @Autowired
    RestTransport restTransport;

    public void execute(Context context) throws PeException {
        Map<Object,Object> request=new HashMap();
        request.putAll(context.getDataMap());
        System.out.println(context.getDataMap().toString());
        request.put("_TransactionId", context.getTransactionId());
        request.put("_ServiceName", "PE-CLOUD-SERVER");
        request.put("userId", "2222");




        Map<?, ?> response=(Map<?, ?>) restTransport.submit(request);
        context.setDataMap(response);
    }
}
