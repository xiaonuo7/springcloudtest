package com.csii.pe.stream.consumer;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MyProcessor {

    /**
     * 消息生产者的配置
     */
    @Output("myOutput")
    MessageChannel myOutput();

    /**
     * 消息消费者的配置
     */
    @Input("myInput")
    SubscribableChannel myInput();
}

