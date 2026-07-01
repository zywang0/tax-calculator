package org.study.trigger.http.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccumulatedTaxDataDTO {

    private BigDecimal grossIncome;

    private BigDecimal taxFreeIncome;

    private BigDecimal specialDeduction;

    private BigDecimal additionalDeduction;

    private BigDecimal otherDeduction;

    private BigDecimal paidTax;

}
