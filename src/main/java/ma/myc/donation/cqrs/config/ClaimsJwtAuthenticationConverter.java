package ma.myc.donation.cqrs.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * Token -> droits Spring, selon le contrat de claims du SI (ADR 0015) : {@code scope} -> {@code SCOPE_...},
 * {@code permissions} -> permissions telles quelles. Aucune structure propre a un IdP n'est lue.
 */
@Component
public class ClaimsJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private static final String CLAIM_PERMISSIONS = "permissions";
	private static final String CLAIM_PARTY_ID = "party_id";
	private static final String CLAIM_AUTHORIZED_PARTY = "azp";

	private final JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = Stream.concat(
						scopesConverter.convert(jwt).stream(),
						permissions(jwt).stream())
				.toList();
		return new JwtAuthenticationToken(jwt, authorities, principalName(jwt));
	}

	private static List<GrantedAuthority> permissions(Jwt jwt) {
		if (!(jwt.getClaims().get(CLAIM_PERMISSIONS) instanceof Collection<?> permissions)) {
			return List.of();
		}
		return permissions.stream()
				.map(String::valueOf)
				.map(permission -> (GrantedAuthority) new SimpleGrantedAuthority(permission))
				.toList();
	}

	private static String principalName(Jwt jwt) {
		String partyId = jwt.getClaimAsString(CLAIM_PARTY_ID);
		return partyId != null ? partyId : jwt.getClaimAsString(CLAIM_AUTHORIZED_PARTY);
	}
}
