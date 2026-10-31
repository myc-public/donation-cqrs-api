package ma.myc.donation.cqrs.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ma.myc.donation.cqrs.config.ClaimsJwtAuthenticationConverter;
import ma.myc.donation.cqrs.config.SecurityConfig;
import ma.myc.donation.cqrs.search.DonationCriteria;
import ma.myc.donation.cqrs.search.DonationView;
import ma.myc.donation.cqrs.search.PageResponse;
import ma.myc.donation.cqrs.search.SearchService;

@WebMvcTest(controllers = SearchController.class)
@Import({SecurityConfig.class, ClaimsJwtAuthenticationConverter.class, ErrorHandlingAdvice.class})
class SearchControllerTest {

	private static final String SCOPE = "SCOPE_donation:read";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private SearchService searchService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	private static JwtRequestPostProcessor token(String... authorities) {
		return jwt().authorities(java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
	}

	@Test
	@DisplayName("sans token : 401, le service n'est pas appele")
	void noToken_401() throws Exception {
		mvc.perform(get("/api/v1/search/donations")).andExpect(status().isUnauthorized());

		verifyNoInteractions(searchService);
	}

	@Test
	@DisplayName("sans scope : 403, meme avec la permission")
	void noScope_403() throws Exception {
		mvc.perform(get("/api/v1/search/donations").with(token("donation:list"))).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("sans permission : 403, meme avec le scope")
	void noPermission_403() throws Exception {
		mvc.perform(get("/api/v1/search/donations").with(token(SCOPE))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/search/donors").with(token(SCOPE, "donation:list"))).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("toute autre route, ou une ecriture : refusee")
	void otherRoutes_denied() throws Exception {
		mvc.perform(post("/api/v1/search/donations").with(token(SCOPE, "donation:list"))).andExpect(status().isForbidden());
		mvc.perform(get("/management/env").with(token(SCOPE))).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("recherche : 200, resultats a plat, criteres transmis")
	void donations_200() throws Exception {
		var view = new DonationView("g-1", "FOOD", true, null, "2026-10-31T11:07:00Z", "d-1", "MA", "2026-10-31T11:07:01Z");
		when(searchService.donations(any(), eq(0), eq(20))).thenReturn(new PageResponse<>(List.of(view), 1, 0, 20));

		mvc.perform(get("/api/v1/search/donations").param("category", "FOOD").with(token(SCOPE, "donation:list")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value("g-1"))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(content().string(not(containsString("eventId"))));

		verify(searchService).donations(new DonationCriteria("FOOD", null, null, null, null), 0, 20);
	}

	@Test
	@DisplayName("pagination : size > 100 ou fenetre > 10 000 -> 400")
	void pagination_bounded() throws Exception {
		mvc.perform(get("/api/v1/search/donors").param("size", "101").with(token(SCOPE, "donor:list")))
				.andExpect(status().isBadRequest());
		mvc.perform(get("/api/v1/search/donors").param("page", "100").param("size", "100").with(token(SCOPE, "donor:list")))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(searchService);
	}

	@Test
	@DisplayName("parametre invalide : 400")
	void invalidParameter_400() throws Exception {
		mvc.perform(get("/api/v1/search/donations").param("donorId", "pas-un-uuid").with(token(SCOPE, "donation:list")))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Elasticsearch indisponible : 503, sans detail technique")
	void elasticsearchDown_503() throws Exception {
		when(searchService.donations(any(), anyInt(), anyInt())).thenThrow(new UncheckedIOException(new IOException("connection refused")));

		mvc.perform(get("/api/v1/search/donations").with(token(SCOPE, "donation:list")))
				.andExpect(status().isServiceUnavailable())
				.andExpect(content().string(not(containsString("connection refused"))));
	}
}
