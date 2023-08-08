package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.Quantity;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.flare.model.fhir.QueryParams.*;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.GREATER_EQUAL;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.LESS_EQUAL;
import static org.assertj.core.api.Assertions.assertThat;

class ExpandedCriterionTest {

    static final TermCode UNIT = new TermCode("http://unitsofmeasure.org", "ug/dL", "ug/dL");
    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1", "Frontallappen");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000", "Severe");
    static final TermCode CORTISOL = TermCode.of("http://loinc.org", "2143-6", "Cortisol");
    static final BigDecimal DECIMAL_LB = BigDecimal.valueOf(17.9);
    static final BigDecimal DECIMAL_UB = BigDecimal.valueOf(22);
    static final BigDecimal DECIMAL_LB_1 = BigDecimal.valueOf(23);
    static final BigDecimal DECIMAL_UB_1 = BigDecimal.valueOf(42);
    static final BigDecimal DECIMAL_LB_2 = BigDecimal.valueOf(30);
    static final BigDecimal DECIMAL_UB_2 = BigDecimal.valueOf(43.5);
    static final BigDecimal DECIMAL_1 = BigDecimal.valueOf(7.3);
    static final BigDecimal DECIMAL_2 = BigDecimal.valueOf(10);
    static final LocalDate LOCAL_DATE = LocalDate.of(1990, 10, 2);
    static final LocalDate LOCAL_DATE_1 = LocalDate.of(1990, 10, 2);
    static final LocalDate LOCAL_DATE_2 = LocalDate.of(2004, 4, 30);
    static final TermCode COMPOSITE_CODE = new TermCode("http://loing.org", "8480-6", "Sistolic Bloodpressure");
    static final String COMPONENT_CODE_VALUE_QUANTITY = "component-code-value-quantity";
    static final String COMPONENT_CODE_VALUE_CONCEPT = "component-code-value-concept";

    @Test
    void toQuery() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.of("code", conceptValue(C71_1), null)));
    }

    @Test
    void toQuery_withOneConceptFilter() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", conceptValue(C71_1), null)
                .appendParam("verification-status", conceptValue(CONFIRMED), null)));
    }

    @Test
    void toQuery_withTwoConceptFilters() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED, null))
                .appendFilter(new ExpandedConceptFilter("severity", SEVERE, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", conceptValue(C71_1), null)
                .appendParam("verification-status", conceptValue(CONFIRMED), null)
                .appendParam("severity", conceptValue(SEVERE), null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneComparatorFilter(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedQuantityComparatorFilter("value-quantity", comparator,
                        Quantity.of(DECIMAL_1, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("value-quantity", quantityValue(comparator, Quantity.of(DECIMAL_1, UNIT)), null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneComparatorFilter_withoutUnit(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedQuantityComparatorFilter("value-quantity", comparator,
                        Quantity.of(DECIMAL_1), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("value-quantity", quantityValue(comparator, Quantity.of(DECIMAL_1)), null)));
    }

    @ParameterizedTest
    @MethodSource("arityTwoComparatorArgumentProvider")
    void toQuery_withTwoComparatorFilters(Comparator comparator1, Comparator comparator2) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedQuantityComparatorFilter("value-quantity", comparator1,
                        Quantity.of(DECIMAL_1, UNIT), null))
                .appendFilter(new ExpandedQuantityComparatorFilter("value-quantity", comparator2,
                        Quantity.of(DECIMAL_2, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("value-quantity", quantityValue(comparator1, Quantity.of(DECIMAL_1, UNIT)), null)
                .appendParam("value-quantity", quantityValue(comparator2, Quantity.of(DECIMAL_2, UNIT)), null)));
    }

    @Test
    void toQuery_withOneQuantityRangeFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedQuantityRangeFilter("value-quantity", Quantity.of(DECIMAL_LB, UNIT),
                        Quantity.of(DECIMAL_UB, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("value-quantity", quantityValue(GREATER_EQUAL, Quantity.of(DECIMAL_LB, UNIT)), null)
                .appendParam("value-quantity", quantityValue(LESS_EQUAL, Quantity.of(DECIMAL_UB, UNIT)), null)));
    }

    @Test
    void toQuery_withOneQuantityRangeFilter_withoutUnit() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedQuantityRangeFilter("value-quantity", Quantity.of(DECIMAL_LB), Quantity.of(DECIMAL_UB), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("value-quantity", quantityValue(GREATER_EQUAL, Quantity.of(DECIMAL_LB)), null)
                .appendParam("value-quantity", quantityValue(LESS_EQUAL, Quantity.of(DECIMAL_UB)), null)));
    }

    @Test
    void toQuery_withTowQuantityRangeFilters() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedQuantityRangeFilter("value-quantity", Quantity.of(DECIMAL_LB_1, UNIT),
                        Quantity.of(DECIMAL_UB_1, UNIT), null))
                .appendFilter(new ExpandedQuantityRangeFilter("value-quantity", Quantity.of(DECIMAL_LB_2, UNIT),
                        Quantity.of(DECIMAL_UB_2, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("value-quantity", quantityValue(GREATER_EQUAL, Quantity.of(DECIMAL_LB_1, UNIT)), null)
                .appendParam("value-quantity", quantityValue(LESS_EQUAL, Quantity.of(DECIMAL_UB_1, UNIT)), null)
                .appendParam("value-quantity", quantityValue(GREATER_EQUAL, Quantity.of(DECIMAL_LB_2, UNIT)), null)
                .appendParam("value-quantity", quantityValue(LESS_EQUAL, Quantity.of(DECIMAL_UB_2, UNIT)), null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneCompositeComparatorFilter(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedCompositeQuantityComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY,
                        COMPOSITE_CODE, comparator, Quantity.of(DECIMAL_1, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, compositeQuantityValue(COMPOSITE_CODE, comparator,
                        Quantity.of(DECIMAL_1, UNIT)), null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withTwoCompositeQuantityComparatorFilters(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedCompositeQuantityComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY,
                        COMPOSITE_CODE, comparator, Quantity.of(DECIMAL_1, UNIT), null))
                .appendFilter(new ExpandedCompositeQuantityComparatorFilter(COMPONENT_CODE_VALUE_QUANTITY,
                        COMPOSITE_CODE, comparator, Quantity.of(DECIMAL_2, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, compositeQuantityValue(COMPOSITE_CODE, comparator,
                        Quantity.of(DECIMAL_1, UNIT)), null)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, compositeQuantityValue(COMPOSITE_CODE, comparator,
                        Quantity.of(DECIMAL_2, UNIT)), null)));
    }

    @Test
    void toQuery_withOneCompositeQuantityRangeFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedCompositeQuantityRangeFilter(COMPONENT_CODE_VALUE_QUANTITY, COMPOSITE_CODE,
                        Quantity.of(DECIMAL_1, UNIT), Quantity.of(DECIMAL_2, UNIT), null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, compositeQuantityValue(COMPOSITE_CODE, GREATER_EQUAL,
                        Quantity.of(DECIMAL_1, UNIT)), null)
                .appendParam(COMPONENT_CODE_VALUE_QUANTITY, compositeQuantityValue(COMPOSITE_CODE, LESS_EQUAL,
                        Quantity.of(DECIMAL_2, UNIT)), null)));
    }

    @Test
    void toQuery_withOneCompositeConceptFilter() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedCompositeConceptFilter(COMPONENT_CODE_VALUE_CONCEPT, COMPOSITE_CODE, CONFIRMED, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", conceptValue(C71_1), null)
                .appendParam(COMPONENT_CODE_VALUE_CONCEPT, compositeConceptValue(COMPOSITE_CODE, CONFIRMED), null)));
    }

    @Test
    void toQuery_withTwoCompositeConceptFilters() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedCompositeConceptFilter(COMPONENT_CODE_VALUE_CONCEPT, COMPOSITE_CODE, CONFIRMED, null))
                .appendFilter(new ExpandedCompositeConceptFilter(COMPONENT_CODE_VALUE_CONCEPT, COMPOSITE_CODE, SEVERE, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", conceptValue(C71_1), null)
                .appendParam(COMPONENT_CODE_VALUE_CONCEPT, compositeConceptValue(COMPOSITE_CODE, CONFIRMED), null)
                .appendParam(COMPONENT_CODE_VALUE_CONCEPT, compositeConceptValue(COMPOSITE_CODE, SEVERE), null)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_WithDateComparator(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedDateComparatorFilter("birthdate", comparator, LOCAL_DATE, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("birthdate", dateValue(comparator, LOCAL_DATE), null)));
    }

    @Test
    void toQuery_WithDateRange() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedDateRangeFilter("birthdate", LOCAL_DATE_1, LOCAL_DATE_2, null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", conceptValue(CORTISOL), null)
                .appendParam("birthdate", dateValue(GREATER_EQUAL, LOCAL_DATE_1), null)
                .appendParam("birthdate", dateValue(LESS_EQUAL, LOCAL_DATE_2), null)));
    }

    @Test
    void toQuery_Patient_Gender() {
        var criterion = ExpandedCriterion.of("Patient")
                .appendFilter(new ExpandedCodeFilter("gender", "female", null));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Patient", QueryParams.EMPTY
                .appendParam("gender", stringValue("female"), null)));
    }

    static Stream<Arguments> arityTwoComparatorArgumentProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Comparator c1 : Comparator.values()) {
            for (Comparator c2 : Comparator.values()) {
                argumentBuilder.add(Arguments.of(c1, c2));
            }
        }
        return argumentBuilder.build();
    }
}
