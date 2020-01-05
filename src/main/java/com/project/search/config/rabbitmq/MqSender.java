package com.project.search.config.rabbitmq;

import com.project.search.constants.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
/**
  如果消息没有到exchange,则confirm回调,ack=false
  如果消息到达exchange,则confirm回调,ack=true
  exchange到queue成功,则不回调return
  exchange到queue失败,则回调return
 */
public class MqSender implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback{
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String topicKey,String message){
        //生成唯一标识id
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        System.out.println("消息id:" + correlationData.getId());
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getHeaders().put("desc","desc..");
        Message msg = new Message(message.getBytes(),messageProperties);
        this.rabbitTemplate.convertAndSend(MqConstants.ES_EXCHANGE_NAME, topicKey, msg, correlationData);
    }

    @Override
    public void confirm(@Nullable CorrelationData correlationData, boolean ack,String cause) {
        if (ack) {
            // TODO 入库
            log.info("消息发送确认成功{}",correlationData.getId());
        } else {
            log.info("消息发送确认失败{}"+ cause,correlationData.getId());
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("return--message:" + new String(message.getBody()) + ",replyCode:" + replyCode
                + ",replyText:" + replyText + ",exchange:" + exchange + ",routingKey:" + routingKey);
    }
}
