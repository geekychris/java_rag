package com.example.ragservice.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.host:localhost}")
    private String host;

    @Value("${opensearch.port:30920}")
    private int port;

    @Value("${opensearch.scheme:http}")
    private String scheme;

    @Value("${opensearch.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${opensearch.socket-timeout:60000}")
    private int socketTimeout;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
            RestClient.builder(new HttpHost(host, port, scheme))
                .setRequestConfigCallback(requestConfigBuilder ->
                    requestConfigBuilder
                        .setConnectTimeout(connectionTimeout)
                        .setSocketTimeout(socketTimeout))
        );
    }

    @Bean
    public OpenSearchClient openSearchClient(RestHighLevelClient restHighLevelClient) {
        RestClientTransport transport = new RestClientTransport(
            restHighLevelClient.getLowLevelClient(),
            new JacksonJsonpMapper()
        );
        return new OpenSearchClient(transport);
    }
}
