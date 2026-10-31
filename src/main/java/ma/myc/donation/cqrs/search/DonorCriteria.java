package ma.myc.donation.cqrs.search;

public record DonorCriteria(String q, String email, String country) {
}
