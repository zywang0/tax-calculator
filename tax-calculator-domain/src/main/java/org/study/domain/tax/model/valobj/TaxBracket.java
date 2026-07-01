package org.study.domain.tax.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxBracket {

    private Integer level;

    private BigDecimal minExclusive;

    private BigDecimal maxInclusive;

    private BigDecimal taxRate;

    private BigDecimal quickDeduction;

    public boolean matches(BigDecimal taxableIncome) {
        boolean greaterThanMin = taxableIncome.compareTo(minExclusive) > 0;
        boolean lessThanMax = maxInclusive == null || taxableIncome.compareTo(maxInclusive) <= 0;
        return greaterThanMin && lessThanMax;
    }

}
