package com.killrvideo.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.OpenSearchTransport;

import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import org.springframework.stereotype.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.killrvideo.dto.Video;

@Repository
public class VideoSearchDao {
	
	private static final Logger logger = LoggerFactory.getLogger(VideoDao.class);

	private static String indexName;
	
	private OpenSearchClient client;
	
	public VideoSearchDao() {
        String opensearchHost = System.getenv("OPENSEARCH_HOST");
        String opensearchScheme = "https";

        String username = System.getenv("OPENSEARCH_USERNAME");
        String password = System.getenv("OPENSEARCH_PASSWORD");

        indexName = "videos";
        int opensearchPort = 0;

        if (System.getenv("OPENSEARCH_PORT") != null) {
        	opensearchPort = Integer.parseInt(System.getenv("OPENSEARCH_PORT"));
        } else {
        	opensearchPort = 9200;
        }
        
        try {
	    	final HttpHost host = new HttpHost(opensearchScheme, opensearchHost, opensearchPort);
	        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        final var sslContext = SSLContextBuilder.create().loadTrustMaterial(null, (chains, authType) -> true).build();
	        
	        credentialsProvider.setCredentials(
	            new AuthScope(host),
	            new UsernamePasswordCredentials(username, password.toCharArray())
	        );
	
	        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
        		.builder(host)
        		.setMapper(new JacksonJsonpMapper())
                .setHttpClientConfigCallback(httpClientBuilder -> {

                    // Disable SSL/TLS verification as our local testing clusters use self-signed certificates
                    final var tlsStrategy = ClientTlsStrategyBuilder.create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();

                    final var connectionManager = PoolingAsyncClientConnectionManagerBuilder.create().setTlsStrategy(tlsStrategy).build();

                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider).setConnectionManager(connectionManager);
                })
        		.build();

	        client = new OpenSearchClient(transport);
	        
        } catch (Exception ex) {
        	logger.error("Unable to connect to OpenSearch: {}", ex.getMessage());
        }
	}
	
	public Optional<List<Video>> searchVideos(String text, int limit) {
        List<Video> returnVal = new ArrayList<>();
		
		Query searchQuery = Query.of(q -> q
                .match(m -> m
                    .field("description")
                    .query(FieldValue.of(text))
                )
            );

		try {
			SearchResponse<Video> resp = client.search(s -> s
				.index(indexName)
				.size(limit)
				.query(searchQuery)
				, Video.class);

			for (Hit<Video> hit : resp.hits().hits()) {
				returnVal.add(hit.source());
			}

		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		return Optional.of(returnVal);
	}
}
