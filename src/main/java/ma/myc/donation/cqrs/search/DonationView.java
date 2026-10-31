package ma.myc.donation.cqrs.search;

import java.math.BigDecimal;

/**
 * Vue de lecture, sans enveloppe d'evenement. {@code indexedAt} rappelle la coherence a terme (ADR 0036).
 */
public record DonationView(String id, String category, Boolean recurring, BigDecimal amount, String timestamp,
                           String donorId, String country, String indexedAt) {
}
