package com.csii.pe.stream.consumer;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(MyProcessor.class)
public class MessageListener {

    //监听binding中的消息
    @StreamListener("myInput")
    public void input(String message) {
        System.out.println("获取到消息: "+message);
    }

}