package ma.myc.donation.cqrs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Un seul emetteur : le realm myc-internal, back-office (ADR 0036). Un token client (myc-customers) est refuse.
 */
@ConfigurationProperties(prefix = "myc.oidc")
public record OidcProps(String issuerUri, String jwkSetUri, String audience) {
}
