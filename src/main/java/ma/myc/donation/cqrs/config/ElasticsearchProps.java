package ma.myc.donation.cqrs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myc.elasticsearch")
public record ElasticsearchProps(String url, String username, String password) {
}
