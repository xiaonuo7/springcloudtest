package com.csii.pe.service.test;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.csii.pe.service.ServiceApplication;
import com.csii.pe.service.mq.sender.ErrorSender;
import com.csii.pe.service.stream.provider.MessageSender;

/**
 * Direct交换器
 * Producer测试。
 * 注意：
 * 在rabbitmq中，consumer都是listener监听模式消费消息的。
 * 一般来说，在开发的时候，都是先启动consumer，确定有什么exchange、queue、routing-key，然后再启动producer。
 * 然后再启动producer发送消息，。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=ServiceApplication.class)
public class QueueTest {
	
    @Autowired
    private ErrorSender sender;
    @Autowired
    private MessageSender streamSender;
    
    /*
     * 测试消息队列
     */
//    @Test
//    public void testSend()throws Exception{
//        Long id = 1L;
//        while(true){
//            Thread.sleep(1000);
//            this.sender.send(String.valueOf(System.currentTimeMillis()));
//            id++;
//        }
//    }
    //测试stream发送消息
    @Test
    public void streamTestSend()throws Exception{
        Long id = 1L;
        while(true){
            Thread.sleep(1000);
            this.streamSender.send(String.valueOf(System.currentTimeMillis()));
            id++;
        }
    }
}