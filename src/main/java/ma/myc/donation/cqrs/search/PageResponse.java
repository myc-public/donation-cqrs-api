package ma.myc.donation.cqrs.search;

import java.util.List;

public record PageResponse<T>(List<T> items, long total, int page, int size) {
}
