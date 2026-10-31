package ma.myc.donation.cqrs.search;

/**
 * Vue de lecture, sans enveloppe d'evenement. Donnees personnelles : jamais dans les logs.
 */
public record DonorView(String id, String lastName, String firstName, String email, String dateOfBirth,
                        String country, String createdAt, String indexedAt) {
}
