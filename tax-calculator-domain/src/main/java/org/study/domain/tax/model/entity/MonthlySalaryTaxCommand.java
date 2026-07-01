package org.study.domain.tax.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.study.domain.tax.model.valobj.AdditionalDeduction;
import org.study.domain.tax.model.valobj.SpecialDeduction;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalaryTaxCommand {

    private Integer taxYear;

    private Integer month;

    private String ruleVersion;

    private BigDecimal grossIncome;

    private BigDecimal taxFreeIncome;

    private SpecialDeduction specialDeduction;

    private AdditionalDeduction additionalDeduction;

    private BigDecimal otherDeduction;

    private AccumulatedTaxData accumulatedBeforeCurrentMonth;

}
