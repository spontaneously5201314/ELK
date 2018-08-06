package com.cmcm.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Spontaneously
 * @time 2018-08-05 下午3:45
 */
@Configuration
public class ElasticSearchConfiguration {

    @Bean
    public TransportClient client() {
        InetSocketTransportAddress node = null;
        try {
            node = new InetSocketTransportAddress(
                    InetAddress.getByName("localhost"), 9300
            );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .build();

        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);

        return client;
    }
}
