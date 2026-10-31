package ma.myc.donation.cqrs.api;

import java.time.Instant;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import ma.myc.donation.cqrs.search.DonationCriteria;
import ma.myc.donation.cqrs.search.DonationView;
import ma.myc.donation.cqrs.search.DonorCriteria;
import ma.myc.donation.cqrs.search.DonorView;
import ma.myc.donation.cqrs.search.PageResponse;
import ma.myc.donation.cqrs.search.SearchService;

/**
 * Recherche du back-office, en coherence a terme. Memes permissions que les listes de donation-api (ADR 0036).
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

	/** Au-dela, Elasticsearch refuse la page (index.max_result_window). */
	static final int MAX_WINDOW = 10_000;
	private static final int MAX_SIZE = 100;

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/donations")
	@PreAuthorize("hasAuthority('donation:list')")
	public PageResponse<DonationView> donations(
			@RequestParam(required = false) @Size(max = 80) String category,
			@RequestParam(required = false) @Size(max = 80) String country,
			@RequestParam(required = false) UUID donorId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(MAX_SIZE) int size) {
		checkWindow(page, size);
		return searchService.donations(new DonationCriteria(category, country, donorId, from, to), page, size);
	}

	@GetMapping("/donors")
	@PreAuthorize("hasAuthority('donor:list')")
	public PageResponse<DonorView> donors(
			@RequestParam(required = false) @Size(max = 120) String q,
			@RequestParam(required = false) @Size(max = 320) String email,
			@RequestParam(required = false) @Size(max = 80) String country,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(MAX_SIZE) int size) {
		checkWindow(page, size);
		return searchService.donors(new DonorCriteria(q, email, country), page, size);
	}

	private static void checkWindow(int page, int size) {
		if ((long) (page + 1) * size > MAX_WINDOW) {
			throw new IllegalArgumentException("Pagination au-dela de " + MAX_WINDOW + " resultats");
		}
	}
}
