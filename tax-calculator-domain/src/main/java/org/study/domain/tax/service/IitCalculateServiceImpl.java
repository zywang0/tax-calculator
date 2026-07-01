package org.study.domain.tax.service;

import org.apache.commons.lang3.StringUtils;
import org.study.domain.tax.model.aggregate.TaxRule;
import org.study.domain.tax.model.entity.AccumulatedTaxData;
import org.study.domain.tax.model.entity.MonthlySalaryTaxCommand;
import org.study.domain.tax.model.entity.MonthlySalaryTaxResult;
import org.study.domain.tax.model.valobj.AdditionalDeduction;
import org.study.domain.tax.model.valobj.SpecialDeduction;
import org.study.domain.tax.model.valobj.TaxBracket;
import org.study.domain.tax.repository.TaxRuleRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class IitCalculateServiceImpl implements IitCalculateService {

    private final TaxRuleRepository taxRuleRepository;

    public IitCalculateServiceImpl(TaxRuleRepository taxRuleRepository) {
        this.taxRuleRepository = taxRuleRepository;
    }

    @Override
    public MonthlySalaryTaxResult calculateMonthlySalaryTax(MonthlySalaryTaxCommand command) {
        validateCommand(command);
        TaxRule taxRule = queryTaxRule(command.getRuleVersion());

        BigDecimal grossIncome = money(command.getGrossIncome());
        BigDecimal taxFreeIncome = money(command.getTaxFreeIncome());
        BigDecimal specialDeduction = money(total(command.getSpecialDeduction()));
        BigDecimal additionalDeduction = money(total(command.getAdditionalDeduction()));
        BigDecimal otherDeduction = money(command.getOtherDeduction());
        AccumulatedTaxData accumulatedBefore = safeAccumulated(command.getAccumulatedBeforeCurrentMonth());

        BigDecimal accumulatedGrossIncome = money(accumulatedBefore.getGrossIncome()).add(grossIncome);
        BigDecimal accumulatedTaxFreeIncome = money(accumulatedBefore.getTaxFreeIncome()).add(taxFreeIncome);
        BigDecimal accumulatedBasicDeduction = taxRule.getBasicDeductionMonthly()
                .multiply(new BigDecimal(command.getMonth()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal accumulatedSpecialDeduction = money(accumulatedBefore.getSpecialDeduction()).add(specialDeduction);
        BigDecimal accumulatedAdditionalDeduction = money(accumulatedBefore.getAdditionalDeduction()).add(additionalDeduction);
        BigDecimal accumulatedOtherDeduction = money(accumulatedBefore.getOtherDeduction()).add(otherDeduction);

        BigDecimal taxableIncome = accumulatedGrossIncome
                .subtract(accumulatedTaxFreeIncome)
                .subtract(accumulatedBasicDeduction)
                .subtract(accumulatedSpecialDeduction)
                .subtract(accumulatedAdditionalDeduction)
                .subtract(accumulatedOtherDeduction);
        taxableIncome = taxableIncome.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        TaxBracket taxBracket = taxRule.matchAnnualBracket(taxableIncome);
        BigDecimal accumulatedTaxPayable = taxableIncome
                .multiply(taxBracket.getTaxRate())
                .subtract(taxBracket.getQuickDeduction())
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal paidTaxBeforeCurrentMonth = money(accumulatedBefore.getPaidTax());
        BigDecimal currentTaxPayable = accumulatedTaxPayable
                .subtract(paidTaxBeforeCurrentMonth)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal netIncome = grossIncome
                .subtract(specialDeduction)
                .subtract(currentTaxPayable)
                .setScale(2, RoundingMode.HALF_UP);

        return MonthlySalaryTaxResult.builder()
                .taxYear(command.getTaxYear())
                .month(command.getMonth())
                .ruleVersion(taxRule.getRuleVersion())
                .grossIncome(grossIncome)
                .taxFreeIncome(taxFreeIncome)
                .specialDeduction(specialDeduction)
                .additionalDeduction(additionalDeduction)
                .otherDeduction(otherDeduction)
                .accumulatedGrossIncome(accumulatedGrossIncome)
                .accumulatedTaxFreeIncome(accumulatedTaxFreeIncome)
                .accumulatedBasicDeduction(accumulatedBasicDeduction)
                .accumulatedSpecialDeduction(accumulatedSpecialDeduction)
                .accumulatedAdditionalDeduction(accumulatedAdditionalDeduction)
                .accumulatedOtherDeduction(accumulatedOtherDeduction)
                .taxableIncome(taxableIncome)
                .taxRate(taxBracket.getTaxRate())
                .quickDeduction(taxBracket.getQuickDeduction())
                .accumulatedTaxPayable(accumulatedTaxPayable)
                .paidTaxBeforeCurrentMonth(paidTaxBeforeCurrentMonth)
                .currentTaxPayable(currentTaxPayable)
                .netIncome(netIncome)
                .formula("currentTaxPayable = accumulatedTaxPayable - paidTaxBeforeCurrentMonth")
                .build();
    }

    @Override
    public TaxRule queryTaxRule(String ruleVersion) {
        TaxRule taxRule = StringUtils.isBlank(ruleVersion)
                ? taxRuleRepository.defaultRule()
                : taxRuleRepository.findByRuleVersion(ruleVersion);
        if (taxRule == null) {
            throw new IllegalArgumentException("税率规则版本不存在");
        }
        return taxRule;
    }

    private void validateCommand(MonthlySalaryTaxCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (command.getMonth() == null || command.getMonth() < 1 || command.getMonth() > 12) {
            throw new IllegalArgumentException("月份范围必须为 1 到 12");
        }
        if (command.getTaxYear() == null || command.getTaxYear() < 2019) {
            throw new IllegalArgumentException("计税年份不能早于 2019");
        }
        assertNotNegative(command.getGrossIncome(), "税前收入不能为负数");
        assertNotNegative(command.getTaxFreeIncome(), "免税收入不能为负数");
        assertNotNegative(command.getOtherDeduction(), "其他扣除不能为负数");
        assertNotNegative(total(command.getSpecialDeduction()), "专项扣除不能为负数");
        assertNotNegative(total(command.getAdditionalDeduction()), "专项附加扣除不能为负数");
        AccumulatedTaxData accumulated = command.getAccumulatedBeforeCurrentMonth();
        if (accumulated != null) {
            assertNotNegative(accumulated.getGrossIncome(), "累计税前收入不能为负数");
            assertNotNegative(accumulated.getTaxFreeIncome(), "累计免税收入不能为负数");
            assertNotNegative(accumulated.getSpecialDeduction(), "累计专项扣除不能为负数");
            assertNotNegative(accumulated.getAdditionalDeduction(), "累计专项附加扣除不能为负数");
            assertNotNegative(accumulated.getOtherDeduction(), "累计其他扣除不能为负数");
            assertNotNegative(accumulated.getPaidTax(), "累计已缴税额不能为负数");
        }
    }

    private void assertNotNegative(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private AccumulatedTaxData safeAccumulated(AccumulatedTaxData accumulatedTaxData) {
        if (accumulatedTaxData == null) {
            return AccumulatedTaxData.builder().build();
        }
        return accumulatedTaxData;
    }

    private BigDecimal total(SpecialDeduction specialDeduction) {
        return specialDeduction == null ? BigDecimal.ZERO : specialDeduction.total();
    }

    private BigDecimal total(AdditionalDeduction additionalDeduction) {
        return additionalDeduction == null ? BigDecimal.ZERO : additionalDeduction.total();
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

}
