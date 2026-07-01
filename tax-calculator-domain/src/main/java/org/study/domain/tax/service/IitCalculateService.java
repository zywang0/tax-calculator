package org.study.domain.tax.service;

import org.study.domain.tax.model.aggregate.TaxRule;
import org.study.domain.tax.model.entity.MonthlySalaryTaxCommand;
import org.study.domain.tax.model.entity.MonthlySalaryTaxResult;

public interface IitCalculateService {

    MonthlySalaryTaxResult calculateMonthlySalaryTax(MonthlySalaryTaxCommand command);

    TaxRule queryTaxRule(String ruleVersion);

}
