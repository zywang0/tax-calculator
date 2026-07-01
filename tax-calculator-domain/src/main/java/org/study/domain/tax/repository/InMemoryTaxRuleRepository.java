package org.study.domain.tax.repository;

import org.study.domain.tax.model.aggregate.TaxRule;
import org.study.domain.tax.model.valobj.TaxBracket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTaxRuleRepository implements TaxRuleRepository {

    public static final String DEFAULT_RULE_VERSION = "CN_IIT_2026";

    private final Map<String, TaxRule> ruleMap;

    public InMemoryTaxRuleRepository() {
        Map<String, TaxRule> rules = new HashMap<>();
        TaxRule defaultRule = buildDefaultRule();
        rules.put(defaultRule.getRuleVersion(), defaultRule);
        this.ruleMap = Collections.unmodifiableMap(rules);
    }

    @Override
    public TaxRule findByRuleVersion(String ruleVersion) {
        return ruleMap.get(ruleVersion);
    }

    @Override
    public TaxRule defaultRule() {
        return ruleMap.get(DEFAULT_RULE_VERSION);
    }

    private TaxRule buildDefaultRule() {
        return TaxRule.builder()
                .ruleVersion(DEFAULT_RULE_VERSION)
                .countryOrRegion("CN")
                .effectiveFrom("2026-01-01")
                .effectiveTo("2026-12-31")
                .basicDeductionMonthly(money("5000"))
                .basicDeductionAnnual(money("60000"))
                .annualTaxBrackets(Arrays.asList(
                        bracket(1, "0", "36000", "0.03", "0"),
                        bracket(2, "36000", "144000", "0.10", "2520"),
                        bracket(3, "144000", "300000", "0.20", "16920"),
                        bracket(4, "300000", "420000", "0.25", "31920"),
                        bracket(5, "420000", "660000", "0.30", "52920"),
                        bracket(6, "660000", "960000", "0.35", "85920"),
                        bracket(7, "960000", null, "0.45", "181920")
                ))
                .monthlyTaxBrackets(Arrays.asList(
                        bracket(1, "0", "3000", "0.03", "0"),
                        bracket(2, "3000", "12000", "0.10", "210"),
                        bracket(3, "12000", "25000", "0.20", "1410"),
                        bracket(4, "25000", "35000", "0.25", "2660"),
                        bracket(5, "35000", "55000", "0.30", "4410"),
                        bracket(6, "55000", "80000", "0.35", "7160"),
                        bracket(7, "80000", null, "0.45", "15160")
                ))
                .build();
    }

    private TaxBracket bracket(Integer level, String minExclusive, String maxInclusive, String taxRate, String quickDeduction) {
        return TaxBracket.builder()
                .level(level)
                .minExclusive(money(minExclusive))
                .maxInclusive(maxInclusive == null ? null : money(maxInclusive))
                .taxRate(new BigDecimal(taxRate))
                .quickDeduction(money(quickDeduction))
                .build();
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

}
