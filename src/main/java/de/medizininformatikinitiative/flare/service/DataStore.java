package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.Query;
import de.medizininformatikinitiative.flare.model.fhir.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Component
public class DataStore implements QueryService {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);

    private final WebClient client;
    private final int pageCount;

    public DataStore(@Qualifier("dataStoreClient") WebClient client,
                     @Value("${app.dataStore.pageCount}") int pageCount) {
        this.client = Objects.requireNonNull(client);
        this.pageCount = pageCount;
    }

    public CompletableFuture<Set<String>> execute(Query query) {
        logger.debug("search for {}s with query: {}", query.type(), query.params());
        return client.post()
                .uri("/{type}/_search", query.type())
                .contentType(APPLICATION_FORM_URLENCODED)
                .bodyValue(String.join("&", query.params(), extraQueryParams(query.type())))
                .retrieve()
                .bodyToFlux(Bundle.class)
                .expand(bundle -> bundle.linkWithRel("next")
                        .map(link -> fetchPage(link.url()))
                        .orElse(Mono.empty()))
                .flatMap(
                        bundle -> Flux.fromStream(bundle.entry().stream().map(e -> e.resource().patientId())))
                .collect(Collectors.toSet())
                .toFuture();
    }

    private Mono<Bundle> fetchPage(String url) {
        logger.trace("fetch page {}", url);
        return client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Bundle.class);
    }

    private String extraQueryParams(String type) {
        return "_elements=%s&_count=%d".formatted(queryElements(type), pageCount);
    }

    /**
     * The elements the FHIR server should return in resources. For patients the id is sufficient and
     * for all other resource types, we need the subject reference.
     */
    private String queryElements(String type) {
        return switch (type) {
            case "Patient" -> "id";
            case "Immunization" -> "patient";
            case "Consent" -> "patient";
            default -> "subject";
        };
    }
}