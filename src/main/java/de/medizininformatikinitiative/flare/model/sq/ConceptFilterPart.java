package de.medizininformatikinitiative.flare.model.sq;

import de.medizininformatikinitiative.flare.model.mapping.FilterMapping;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCodeFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedConceptFilter;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

public record ConceptFilterPart(List<TermCode> concepts) implements FilterPart {

    public ConceptFilterPart {
        concepts = List.copyOf(concepts);
    }

    /**
     * Creates a concept attribute filterPart.
     *
     * @param concept the first selected concept
     * @return the concept attribute filterPart
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    static ConceptFilterPart of(TermCode concept) {
        return new ConceptFilterPart(List.of(concept));
    }

    ConceptFilterPart appendConcept(TermCode concept) {
        var concepts = new LinkedList<>(this.concepts);
        concepts.add(concept);
        return new ConceptFilterPart(concepts);
    }

    @Override
    public Flux<ExpandedFilter> expand(FilterMapping filterMapping) {
        return Flux.fromIterable(concepts)
                .flatMap(concept -> switch (filterMapping.type()) {
                    case CODE -> Mono.just(new ExpandedCodeFilter(filterMapping.searchParameter(), concept.code()));
                    case CODING -> Mono.just(new ExpandedConceptFilter(filterMapping.searchParameter(), concept));
                });
    }
}
