package org.study.trigger.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TaxRuleResponseDTO {

    private String ruleVersion;

    private String countryOrRegion;

    private String effectiveFrom;

    private String effectiveTo;

    private BigDecimal basicDeductionMonthly;

    private BigDecimal basicDeductionAnnual;

    private List<TaxBracketDTO> annualTaxBrackets;

    private List<TaxBracketDTO> monthlyTaxBrackets;

}
