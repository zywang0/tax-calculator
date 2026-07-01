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
public class SpecialDeduction {

    private BigDecimal pension;

    private BigDecimal medical;

    private BigDecimal unemployment;

    private BigDecimal housingFund;

    public BigDecimal total() {
        return valueOf(pension)
                .add(valueOf(medical))
                .add(valueOf(unemployment))
                .add(valueOf(housingFund));
    }

    private BigDecimal valueOf(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
