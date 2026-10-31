package ma.myc.donation.cqrs.search;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

/**
 * Criteres -> requete Elasticsearch. Les valeurs passent par des clauses typees : aucune concatenation.
 */
final class SearchQueries {

	private SearchQueries() {
	}

	static Query donations(DonationCriteria criteria) {
		List<Query> filters = new ArrayList<>();
		addTerm(filters, "payload.category", criteria.category());
		addTerm(filters, "payload.country", criteria.country());
		addTerm(filters, "payload.donorID", criteria.donorId() != null ? criteria.donorId().toString() : null);
		if (criteria.from() != null || criteria.to() != null) {
			filters.add(range("payload.timestamp", criteria.from(), criteria.to()));
		}
		return Query.of(q -> q.bool(b -> b.filter(filters)));
	}

	static Query donors(DonorCriteria criteria) {
		List<Query> filters = new ArrayList<>();
		addTerm(filters, "payload.email", criteria.email());
		addTerm(filters, "payload.country", criteria.country());
		return Query.of(q -> q.bool(b -> {
			b.filter(filters);
			if (criteria.q() != null && !criteria.q().isBlank()) {
				b.must(m -> m.multiMatch(mm -> mm.query(criteria.q()).fields("payload.lastName", "payload.firstName")));
			}
			return b;
		}));
	}

	private static void addTerm(List<Query> filters, String field, String value) {
		if (value != null && !value.isBlank()) {
			filters.add(Query.of(q -> q.term(t -> t.field(field).value(value))));
		}
	}

	private static Query range(String field, Instant from, Instant to) {
		return Query.of(q -> q.range(r -> r.date(d -> {
			d.field(field);
			if (from != null) {
				d.gte(from.toString());
			}
			if (to != null) {
				d.lte(to.toString());
			}
			return d;
		})));
	}
}
