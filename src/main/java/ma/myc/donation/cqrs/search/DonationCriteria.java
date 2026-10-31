package ma.myc.donation.cqrs.search;

import java.time.Instant;
import java.util.UUID;

public record DonationCriteria(String category, String country, UUID donorId, Instant from, Instant to) {
}
