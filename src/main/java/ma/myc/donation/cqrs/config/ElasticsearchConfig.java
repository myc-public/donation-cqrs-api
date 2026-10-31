package ma.myc.donation.cqrs.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Client declare ici, pas par l'auto-configuration de Boot : celle-ci vise le client 9, refuse par un serveur 8.19.
 */
@Configuration
public class ElasticsearchConfig {

	@Bean(destroyMethod = "close")
	RestClient elasticsearchRestClient(ElasticsearchProps props) {
		var credentials = new BasicCredentialsProvider();
		credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(props.username(), props.password()));
		return RestClient.builder(HttpHost.create(props.url()))
				.setHttpClientConfigCallback(http -> http.setDefaultCredentialsProvider(credentials))
				.build();
	}

	@Bean
	ElasticsearchClient elasticsearchClient(RestClient restClient) {
		return new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
	}
}
