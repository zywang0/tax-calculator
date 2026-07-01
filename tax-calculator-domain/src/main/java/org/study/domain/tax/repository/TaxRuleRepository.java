package org.study.domain.tax.repository;

import org.study.domain.tax.model.aggregate.TaxRule;

public interface TaxRuleRepository {

    TaxRule findByRuleVersion(String ruleVersion);

    TaxRule defaultRule();

}
