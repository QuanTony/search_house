package com.project.search.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class ElasticSearchConfig {
    @Value("${es.cluster.host}")
    private String esMasterHost;

    @Value("${es.cluster.host1}")
    private String esSlaveHost1;

    @Value("${es.cluster.host2}")
    private String esSlaveHost2;

    @Value("${es.cluster.name}")
    private String clusterName;

    @Value("${es.master.port}")
    private int masterPost;

    @Value("${es.slave1.port}")
    private int slavePost1;

    @Value("${es.slave2.port}")
    private int slavePost2;

    @Bean
    public TransportClient esClient() throws UnknownHostException {
        TransportClient client = null;
        try{
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName)
//                    .put("client.transport.sniff", true)
                    .build();

            InetSocketTransportAddress master = new InetSocketTransportAddress(InetAddress.getByName(esMasterHost), masterPost);
            InetSocketTransportAddress slaveAddress1 = new InetSocketTransportAddress(InetAddress.getByName(esSlaveHost1), slavePost1);
//            InetSocketTransportAddress slaveAddress2 = new InetSocketTransportAddress(InetAddress.getByName(esSlaveHost2), slavePost2);

            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(master)
                    .addTransportAddress(slaveAddress1);
//                    .addTransportAddress(slaveAddress2);
        } catch (Exception e) {
            log.info("初始化es失败:" + e.getMessage());
        }
        return client;
    }
}
