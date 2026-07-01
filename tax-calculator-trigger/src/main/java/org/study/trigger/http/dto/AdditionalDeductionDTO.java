package org.study.trigger.http.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionalDeductionDTO {

    private BigDecimal childrenEducation;

    private BigDecimal continuingEducation;

    private BigDecimal housingLoanInterest;

    private BigDecimal housingRent;

    private BigDecimal elderlyCare;

    private BigDecimal infantCare;

    private BigDecimal seriousIllness;

}
