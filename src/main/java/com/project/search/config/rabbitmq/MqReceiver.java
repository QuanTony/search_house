package com.project.search.config.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.project.search.common.enums.HouseStatus;
import com.project.search.common.utils.JsonMapper;
import com.project.search.constants.MqConstants;
import com.project.search.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MqReceiver {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SearchService searchService;

    @RabbitListener(queues = MqConstants.ES_QUEUE_NAME)
    @RabbitHandler
    public void receive(Message message, Channel channel){
        try{
            try{
                //try catch 捕获异常处理 记录
                Map<String,Object> paramMap = JsonMapper.parseObject(JSON.parse(new String(message.getBody())),Map.class);
                int status = (int)paramMap.get("status");
                Integer houseId = (Integer)paramMap.get("houseId");
                // 上架更新索引 其他情况都要删除索引
                log.info("消费端收到消息:" + houseId + " delivery_tag:" + message.getMessageProperties().getDeliveryTag());
                if (status == HouseStatus.PASSES.getValue()) {
                    searchService.index(houseId);
                } else {
                    searchService.remove(houseId.toString());
                }
//                System.out.println(2/0);测试消息投递失败的代码
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                log.info("消费端收到消息处理完毕:" + houseId);
            } catch (Exception e){
                //第一次失败重试入队 第二次直接丢弃消息
                if (message.getMessageProperties().getRedelivered()) {
//                    channel.basicReject(message.getMessageProperties().getDeliveryTag(), true); // 拒绝消息
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    log.error("消息已重复处理失败,拒绝再次接收...");
                } else {
                    log.error("消息即将丢失请人工处理");
                    //重新放入队列
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                    //抛弃此条消息
//                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    log.info("消费端收到消息处理失败:" + e.getMessage());
                }
            }
        } catch (Exception e){
            log.info("消费端未知错误！！");
        }


    }
}
