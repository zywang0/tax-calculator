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
public class MonthlySalaryTaxResult {

    private Integer taxYear;

    private Integer month;

    private String ruleVersion;

    private BigDecimal grossIncome;

    private BigDecimal taxFreeIncome;

    private BigDecimal specialDeduction;

    private BigDecimal additionalDeduction;

    private BigDecimal otherDeduction;

    private BigDecimal accumulatedGrossIncome;

    private BigDecimal accumulatedTaxFreeIncome;

    private BigDecimal accumulatedBasicDeduction;

    private BigDecimal accumulatedSpecialDeduction;

    private BigDecimal accumulatedAdditionalDeduction;

    private BigDecimal accumulatedOtherDeduction;

    private BigDecimal taxableIncome;

    private BigDecimal taxRate;

    private BigDecimal quickDeduction;

    private BigDecimal accumulatedTaxPayable;

    private BigDecimal paidTaxBeforeCurrentMonth;

    private BigDecimal currentTaxPayable;

    private BigDecimal netIncome;

    private String formula;

}
