package org.study.domain.tax.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.study.domain.tax.model.entity.AccumulatedTaxData;
import org.study.domain.tax.model.entity.MonthlySalaryTaxCommand;
import org.study.domain.tax.model.entity.MonthlySalaryTaxResult;
import org.study.domain.tax.model.valobj.AdditionalDeduction;
import org.study.domain.tax.model.valobj.SpecialDeduction;
import org.study.domain.tax.repository.InMemoryTaxRuleRepository;

import java.math.BigDecimal;

public class IitCalculateServiceTest {

    private IitCalculateService iitCalculateService;

    @Before
    public void setUp() {
        iitCalculateService = new IitCalculateServiceImpl(new InMemoryTaxRuleRepository());
    }

    @Test
    public void testCalculateMonthlySalaryTaxWhenTaxableIncomeIsZero() {
        MonthlySalaryTaxResult result = iitCalculateService.calculateMonthlySalaryTax(MonthlySalaryTaxCommand.builder()
                .taxYear(2026)
                .month(1)
                .ruleVersion(InMemoryTaxRuleRepository.DEFAULT_RULE_VERSION)
                .grossIncome(new BigDecimal("5000"))
                .build());

        Assert.assertEquals(new BigDecimal("0.00"), result.getTaxableIncome());
        Assert.assertEquals(new BigDecimal("0.00"), result.getCurrentTaxPayable());
        Assert.assertEquals(new BigDecimal("5000.00"), result.getNetIncome());
    }

    @Test
    public void testCalculateMonthlySalaryTaxAtFirstAnnualBracketLimit() {
        MonthlySalaryTaxResult result = iitCalculateService.calculateMonthlySalaryTax(MonthlySalaryTaxCommand.builder()
                .taxYear(2026)
                .month(12)
                .ruleVersion(InMemoryTaxRuleRepository.DEFAULT_RULE_VERSION)
                .grossIncome(new BigDecimal("96000"))
                .build());

        Assert.assertEquals(new BigDecimal("36000.00"), result.getTaxableIncome());
        Assert.assertEquals(new BigDecimal("0.03"), result.getTaxRate());
        Assert.assertEquals(new BigDecimal("1080.00"), result.getCurrentTaxPayable());
    }

    @Test
    public void testCalculateMonthlySalaryTaxCrossAnnualBracketLimit() {
        MonthlySalaryTaxResult result = iitCalculateService.calculateMonthlySalaryTax(MonthlySalaryTaxCommand.builder()
                .taxYear(2026)
                .month(12)
                .ruleVersion(InMemoryTaxRuleRepository.DEFAULT_RULE_VERSION)
                .grossIncome(new BigDecimal("96000.01"))
                .build());

        Assert.assertEquals(new BigDecimal("36000.01"), result.getTaxableIncome());
        Assert.assertEquals(new BigDecimal("0.10"), result.getTaxRate());
        Assert.assertEquals(new BigDecimal("1080.00"), result.getCurrentTaxPayable());
    }

    @Test
    public void testCalculateMonthlySalaryTaxWithAccumulatedPaidTax() {
        MonthlySalaryTaxResult result = iitCalculateService.calculateMonthlySalaryTax(MonthlySalaryTaxCommand.builder()
                .taxYear(2026)
                .month(7)
                .ruleVersion(InMemoryTaxRuleRepository.DEFAULT_RULE_VERSION)
                .grossIncome(new BigDecimal("30000"))
                .specialDeduction(SpecialDeduction.builder()
                        .pension(new BigDecimal("2400"))
                        .medical(new BigDecimal("600"))
                        .unemployment(new BigDecimal("150"))
                        .housingFund(new BigDecimal("3600"))
                        .build())
                .additionalDeduction(AdditionalDeduction.builder()
                        .childrenEducation(new BigDecimal("2000"))
                        .housingLoanInterest(new BigDecimal("1000"))
                        .elderlyCare(new BigDecimal("3000"))
                        .build())
                .accumulatedBeforeCurrentMonth(AccumulatedTaxData.builder()
                        .grossIncome(new BigDecimal("180000"))
                        .specialDeduction(new BigDecimal("40500"))
                        .additionalDeduction(new BigDecimal("36000"))
                        .paidTax(new BigDecimal("4000"))
                        .build())
                .build());

        Assert.assertEquals(new BigDecimal("2055.00"), result.getCurrentTaxPayable());
        Assert.assertEquals(new BigDecimal("21195.00"), result.getNetIncome());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateMonthlySalaryTaxWhenMonthIsInvalid() {
        iitCalculateService.calculateMonthlySalaryTax(MonthlySalaryTaxCommand.builder()
                .taxYear(2026)
                .month(13)
                .grossIncome(new BigDecimal("10000"))
                .build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateMonthlySalaryTaxWhenAmountIsNegative() {
        iitCalculateService.calculateMonthlySalaryTax(MonthlySalaryTaxCommand.builder()
                .taxYear(2026)
                .month(1)
                .grossIncome(new BigDecimal("-1"))
                .build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryTaxRuleWhenRuleVersionIsUnknown() {
        iitCalculateService.queryTaxRule("UNKNOWN_RULE");
    }

}
