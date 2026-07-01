package org.study.trigger.http.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecialDeductionDTO {

    private BigDecimal pension;

    private BigDecimal medical;

    private BigDecimal unemployment;

    private BigDecimal housingFund;

}
