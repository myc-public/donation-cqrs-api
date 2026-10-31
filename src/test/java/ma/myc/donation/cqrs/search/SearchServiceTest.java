package ma.myc.donation.cqrs.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.elastic.clients.elasticsearch.core.search.Hit;

class SearchServiceTest {

	@Test
	@DisplayName("toDonation : vue a plat, sans enveloppe d'evenement")
	@SuppressWarnings("rawtypes")
	void toDonation_flattensDocument() {
		Map<String, Object> payload = Map.of("category", "FOOD", "type", true, "amount", 12.5,
				"timestamp", "2026-10-31T11:07:00Z", "donorID", "d-1", "country", "MA");
		Hit<Map> hit = Hit.of(h -> h.id("g-1").index("donations-v1")
				.source(Map.of("eventId", "e-1", "indexedAt", "2026-10-31T11:07:01Z", "payload", payload)));

		DonationView view = SearchService.toDonation(hit);

		assertThat(view).isEqualTo(new DonationView("g-1", "FOOD", true, new BigDecimal("12.5"),
				"2026-10-31T11:07:00Z", "d-1", "MA", "2026-10-31T11:07:01Z"));
	}

	@Test
	@DisplayName("toDonor : vue a plat, identifiant = _id du document")
	@SuppressWarnings("rawtypes")
	void toDonor_flattensDocument() {
		Map<String, Object> payload = Map.of("lastName", "Doe", "firstName", "John", "email", "john@example.test",
				"createdAt", "2026-10-31T11:07:00Z");
		Hit<Map> hit = Hit.of(h -> h.id("d-1").index("donors-v1")
				.source(Map.of("indexedAt", "2026-10-31T11:07:01Z", "payload", payload)));

		DonorView view = SearchService.toDonor(hit);

		assertThat(view).isEqualTo(new DonorView("d-1", "Doe", "John", "john@example.test", null, null,
				"2026-10-31T11:07:00Z", "2026-10-31T11:07:01Z"));
	}
}
