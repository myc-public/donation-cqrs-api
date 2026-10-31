package ma.myc.donation.cqrs.search;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;

/**
 * Cote lecture du CQRS : interroge les alias de lecture, jamais les index ni les alias d'ecriture (ADR 0037, DE34).
 */
@Service
public class SearchService {

	static final String DONATIONS_ALIAS = "donations";
	static final String DONORS_ALIAS = "donors";

	private final ElasticsearchClient client;

	public SearchService(ElasticsearchClient client) {
		this.client = client;
	}

	public PageResponse<DonationView> donations(DonationCriteria criteria, int page, int size) {
		return search(DONATIONS_ALIAS, SearchQueries.donations(criteria), "payload.timestamp", page, size, SearchService::toDonation);
	}

	public PageResponse<DonorView> donors(DonorCriteria criteria, int page, int size) {
		return search(DONORS_ALIAS, SearchQueries.donors(criteria), "payload.createdAt", page, size, SearchService::toDonor);
	}

	@SuppressWarnings("rawtypes")
	private <T> PageResponse<T> search(String alias, Query query, String sortField, int page, int size,
	                                   Function<Hit<Map>, T> mapper) {
		try {
			var response = client.search(s -> s.index(alias)
					.query(query)
					.from(page * size)
					.size(size)
					.trackTotalHits(t -> t.enabled(true))
					.sort(o -> o.field(f -> f.field(sortField).order(SortOrder.Desc))), Map.class);
			List<T> items = response.hits().hits().stream().map(mapper).toList();
			return new PageResponse<>(items, response.hits().total().value(), page, size);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	static DonationView toDonation(Hit<Map> hit) {
		Map<String, Object> source = hit.source();
		Map<String, Object> payload = (Map<String, Object>) source.get("payload");
		Object amount = payload.get("amount");
		return new DonationView(hit.id(), (String) payload.get("category"), (Boolean) payload.get("type"),
				amount != null ? new BigDecimal(amount.toString()) : null, (String) payload.get("timestamp"),
				(String) payload.get("donorID"), (String) payload.get("country"), (String) source.get("indexedAt"));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	static DonorView toDonor(Hit<Map> hit) {
		Map<String, Object> source = hit.source();
		Map<String, Object> payload = (Map<String, Object>) source.get("payload");
		return new DonorView(hit.id(), (String) payload.get("lastName"), (String) payload.get("firstName"),
				(String) payload.get("email"), (String) payload.get("dateOfBirth"), (String) payload.get("country"),
				(String) payload.get("createdAt"), (String) source.get("indexedAt"));
	}
}
