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
public class AdditionalDeduction {

    private BigDecimal childrenEducation;

    private BigDecimal continuingEducation;

    private BigDecimal housingLoanInterest;

    private BigDecimal housingRent;

    private BigDecimal elderlyCare;

    private BigDecimal infantCare;

    private BigDecimal seriousIllness;

    public BigDecimal total() {
        return valueOf(childrenEducation)
                .add(valueOf(continuingEducation))
                .add(valueOf(housingLoanInterest))
                .add(valueOf(housingRent))
                .add(valueOf(elderlyCare))
                .add(valueOf(infantCare))
                .add(valueOf(seriousIllness));
    }

    private BigDecimal valueOf(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
