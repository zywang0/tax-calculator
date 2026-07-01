package org.study.domain.tax.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.study.domain.tax.model.valobj.TaxBracket;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRule {

    private String ruleVersion;

    private String countryOrRegion;

    private String effectiveFrom;

    private String effectiveTo;

    private BigDecimal basicDeductionMonthly;

    private BigDecimal basicDeductionAnnual;

    private List<TaxBracket> annualTaxBrackets;

    private List<TaxBracket> monthlyTaxBrackets;

    public TaxBracket matchAnnualBracket(BigDecimal taxableIncome) {
        BigDecimal safeTaxableIncome = taxableIncome.max(BigDecimal.ZERO);
        return annualTaxBrackets.stream()
                .filter(bracket -> bracket.matches(safeTaxableIncome))
                .findFirst()
                .orElse(annualTaxBrackets.get(0));
    }

}
