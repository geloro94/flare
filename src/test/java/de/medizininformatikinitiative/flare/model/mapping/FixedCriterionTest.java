package de.medizininformatikinitiative.flare.model.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.medizininformatikinitiative.flare.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class FixedCriterionTest {

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = mapper.readValue("""
                {
                    "fhirPath": "status",
                    "searchParameter": "status",
                    "type": "code",
                    "value": [
                        {
                            "code": "active",
                            "display": "Active",
                            "system": "http://hl7.org/fhir/consent-state-codes"
                        }
                    ]
                }
                """, FixedCriterion.class);

        assertThat(criterion).isEqualTo(new FixedCriterion(FilterType.CODE, "status", List.of(TermCode.of(
                "http://hl7.org/fhir/consent-state-codes", "active", "Active"
        )), null));
    }

    @Test
    void expand_WithQuantityCode_fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = mapper.readValue("""
                {
                    "fhirPath": "status",
                    "searchParameter": "status",
                    "type": "composite-quantity-comparator",
                    "value": [
                        {
                            "code": "composite-quantity-comparator",
                            "display": "Active",
                            "system": "http://hl7.org/fhir/consent-state-codes"
                        }
                    ]
                }
                """, FixedCriterion.class);

        assertThat(criterion.expand()).isLeftInstanceOf(FixedCriterionTypeNotExpandableException.class);

    }
}
