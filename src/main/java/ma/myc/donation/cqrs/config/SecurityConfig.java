package ma.myc.donation.cqrs.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource server en lecture seule : audience et emetteur verifies par le decodeur, scope par la chaine,
 * permission fine par {@code @PreAuthorize} (modele C, ADR 0013). Tout le reste est ferme.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	static final String SEARCH_PATTERN = "/api/v1/search/**";
	static final String SCOPE_READ = "SCOPE_donation:read";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, ClaimsJwtAuthenticationConverter converter) throws Exception {
		return http
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(requests -> requests
						.requestMatchers(HttpMethod.GET, SEARCH_PATTERN).hasAuthority(SCOPE_READ)
						.requestMatchers(HttpMethod.GET, "/management/health/**").permitAll()
						.anyRequest().denyAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable) // NOSONAR : stateless, token porteur, lecture seule
				.build();
	}

	@Bean
	JwtDecoder jwtDecoder(OidcProps props) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.jwkSetUri()).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(props.issuerUri()),
				new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, aud -> aud != null && aud.contains(props.audience()))));
		return decoder;
	}
}
