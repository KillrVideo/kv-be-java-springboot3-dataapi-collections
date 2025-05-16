package com.killrvideo.config;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AstraDBConfig {

    @Value("${astra.db.api-endpoint}")
    private String apiEndpoint;

    @Value("${astra.db.application-token}")
    private String applicationToken;

    @Value("${astra.db.namespace}")
    private String namespace;

    @Bean
    public DataAPIClient dataAPIClient() {
        return new DataAPIClient(applicationToken);
    }

    @Bean
    public Database killrVideoDatabase(DataAPIClient dataAPIClient) {
        return dataAPIClient.getDatabase(apiEndpoint, namespace);
    }
} 