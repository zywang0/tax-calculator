package org.study.trigger.http.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlySalaryTaxRequestDTO {

    private Integer taxYear;

    private Integer month;

    private String ruleVersion;

    private BigDecimal grossIncome;

    private BigDecimal taxFreeIncome;

    private SpecialDeductionDTO specialDeduction;

    private AdditionalDeductionDTO additionalDeduction;

    private BigDecimal otherDeduction;

    private AccumulatedTaxDataDTO accumulatedBeforeCurrentMonth;

}
