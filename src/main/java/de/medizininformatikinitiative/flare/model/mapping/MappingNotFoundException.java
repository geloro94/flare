package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.TermCode;

public class MappingNotFoundException extends MappingException {

    public MappingNotFoundException(TermCode termCode) {
        super("Mapping for code %s not found.".formatted(termCode));
    }
}
