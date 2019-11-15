package com.project.search.config.rabbitmq;

import com.project.search.constants.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitmqConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private String port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host}")
    private String virtualHost;

    @Value("${spring.rabbitmq.publisher-confirms}")
    private boolean publisherConfirms;

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(host + ":" + port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        /** 如果要进行消息回调，则这里必须要设置为true */
        connectionFactory.setPublisherConfirms(publisherConfirms);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        //mandatory设置为true之后，生产者通过调用channel.addReturnListener()方法来添加ReturnListener监听器
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    TopicExchange esExchange() {
        return new TopicExchange(MqConstants.ES_EXCHANGE_NAME);
    }

    @Bean
    public Queue esQueue() {
        return new Queue(MqConstants.ES_QUEUE_NAME, true);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(esQueue()).to(esExchange()).with(MqConstants.ES_ROUTING_KEY);
    }

//    @Bean
//    public SimpleMessageListenerContainer messageContainer() {
//		/*Queue[] q = new Queue[queues.split(",").length];
//		for (int i = 0; i < queues.split(",").length; i++) {
//			q[i] = new Queue(queues.split(",")[i]);
//		}*/
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
//        container.setQueues(esQueue());
//        container.setExposeListenerChannel(true);
//        container.setMaxConcurrentConsumers(20);
//        container.setConcurrentConsumers(1);
//        //设置ack开启
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        container.setMessageListener(new ChannelAwareMessageListener() {
//            public void onMessage(Message message, com.rabbitmq.client.Channel channel) throws Exception {
//                try {
//                    log.info("topic:"+message.getMessageProperties().getReceivedRoutingKey()
//                            + "/"+"消费端接收到消息:" + message.getMessageProperties() + ":" + new String(message.getBody()));
//                    // false只确认当前一个消息收到，true确认所有consumer获得的消息
//                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
//                } catch (Exception e) {
//                    log.error("消费数据异常:" + e.getMessage());
//
//                    if (message.getMessageProperties().getRedelivered()) {
//                        log.error("消息已重复处理失败,拒绝再次接收...");
//                        channel.basicReject(message.getMessageProperties().getDeliveryTag(), true); // 拒绝消息
//                    } else {
//                        //重新放入队列
//                        //channel.basicNack(envelope.getDeliveryTag(), false, true);
//                        //抛弃此条消息
//                        //channel.basicNack(envelope.getDeliveryTag(), false, false);
//                        System.out.println();
//                        log.error("消息即将丢失请人工处理");
//                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
//                    }
//                }
//            }
//        });
//        return container;
//    }
}


