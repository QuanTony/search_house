package com.project.search.config.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.project.search.common.enums.HouseStatus;
import com.project.search.common.utils.JsonMapper;
import com.project.search.constants.MqConstants;
import com.project.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
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

    @RabbitListener(queues= MqConstants.ES_QUEUE_NAME)
    public void receive(String message){
        Map<String,Object> paramMap = JsonMapper.parseObject(JSON.parse(message),Map.class);
        int status = (int)paramMap.get("status");
        Integer houseId = (Integer)paramMap.get("houseId");
        // 上架更新索引 其他情况都要删除索引
        log.info("消费端收到消息:" + houseId);
        if (status == HouseStatus.PASSES.getValue()) {
            searchService.index(houseId);
        } else {
            searchService.remove(houseId.toString());
        }
        log.info("消费端收到消息处理完毕:" + houseId);
    }
}
