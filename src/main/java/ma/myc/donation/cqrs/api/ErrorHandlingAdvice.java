package ma.myc.donation.cqrs.api;

import java.io.UncheckedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;

/**
 * Erreurs au format problem+json. Le message d'Elasticsearch n'est jamais renvoye au client.
 */
@RestControllerAdvice
public class ErrorHandlingAdvice extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ErrorHandlingAdvice.class);

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail onBadRequest(IllegalArgumentException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler({UncheckedIOException.class, ElasticsearchException.class})
	ProblemDetail onSearchUnavailable(RuntimeException e) {
		log.error("Elasticsearch indisponible : {}", e.getClass().getSimpleName());
		return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Recherche momentanement indisponible");
	}
}
