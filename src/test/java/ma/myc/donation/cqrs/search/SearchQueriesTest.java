package ma.myc.donation.cqrs.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchQueriesTest {

	@Test
	@DisplayName("donations : sans critere, aucun filtre")
	void donations_noCriteria() {
		String query = SearchQueries.donations(new DonationCriteria(null, null, null, null, null)).toString();

		assertThat(query).doesNotContain("payload.");
	}

	@Test
	@DisplayName("donations : chaque critere devient un filtre sur son champ")
	void donations_allCriteria() {
		var donorId = UUID.randomUUID();
		String query = SearchQueries.donations(new DonationCriteria("FOOD", "MA", donorId,
				Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-10-31T00:00:00Z"))).toString();

		assertThat(query).contains("payload.category", "FOOD", "payload.country", "payload.donorID", donorId.toString(),
				"payload.timestamp", "2026-10-01T00:00:00Z", "2026-10-31T00:00:00Z");
	}

	@Test
	@DisplayName("donors : q cherche dans le nom et le prenom ; email est un filtre exact")
	void donors_allCriteria() {
		String query = SearchQueries.donors(new DonorCriteria("Ali", "a@example.test", "MA")).toString();

		assertThat(query).contains("multi_match", "payload.lastName", "payload.firstName", "Ali", "payload.email");
	}

	@Test
	@DisplayName("donors : un q vide ne filtre rien")
	void donors_blankQuery() {
		assertThat(SearchQueries.donors(new DonorCriteria("  ", null, null)).toString()).doesNotContain("multi_match");
	}
}
