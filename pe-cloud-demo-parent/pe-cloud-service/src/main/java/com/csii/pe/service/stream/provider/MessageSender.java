package com.csii.pe.service.stream.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(MyProcessor.class)
public class MessageSender {

    @Autowired
    @Qualifier(value="myOutput")
    private MessageChannel myOutput;

    //发送消息
    public void send(Object obj) {
    	myOutput.send(MessageBuilder.withPayload(obj).build());
    }

}