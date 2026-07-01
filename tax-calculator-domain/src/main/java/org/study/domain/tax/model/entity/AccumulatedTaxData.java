package org.study.domain.tax.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccumulatedTaxData {

    private BigDecimal grossIncome;

    private BigDecimal taxFreeIncome;

    private BigDecimal specialDeduction;

    private BigDecimal additionalDeduction;

    private BigDecimal otherDeduction;

    private BigDecimal paidTax;

}
