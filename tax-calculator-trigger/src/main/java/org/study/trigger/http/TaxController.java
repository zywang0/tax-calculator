package org.study.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.study.domain.tax.model.aggregate.TaxRule;
import org.study.domain.tax.model.entity.AccumulatedTaxData;
import org.study.domain.tax.model.entity.MonthlySalaryTaxCommand;
import org.study.domain.tax.model.entity.MonthlySalaryTaxResult;
import org.study.domain.tax.model.valobj.AdditionalDeduction;
import org.study.domain.tax.model.valobj.SpecialDeduction;
import org.study.domain.tax.model.valobj.TaxBracket;
import org.study.domain.tax.repository.InMemoryTaxRuleRepository;
import org.study.domain.tax.service.IitCalculateService;
import org.study.domain.tax.service.IitCalculateServiceImpl;
import org.study.trigger.http.dto.AccumulatedTaxDataDTO;
import org.study.trigger.http.dto.AdditionalDeductionDTO;
import org.study.trigger.http.dto.MonthlySalaryTaxRequestDTO;
import org.study.trigger.http.dto.SpecialDeductionDTO;
import org.study.trigger.http.dto.TaxBracketDTO;
import org.study.trigger.http.dto.TaxRuleResponseDTO;
import org.study.types.common.Constants;
import org.study.types.model.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tax/iit")
public class TaxController {

    private final IitCalculateService iitCalculateService = new IitCalculateServiceImpl(new InMemoryTaxRuleRepository());

    @PostMapping("/salary/monthly/calculate")
    public Response<MonthlySalaryTaxResult> calculateMonthlySalaryTax(@RequestBody MonthlySalaryTaxRequestDTO requestDTO) {
        try {
            MonthlySalaryTaxResult result = iitCalculateService.calculateMonthlySalaryTax(toCommand(requestDTO));
            return success(result);
        } catch (IllegalArgumentException e) {
            log.warn("个税单月试算参数错误：{}", e.getMessage());
            return illegalParameter(e.getMessage());
        } catch (Exception e) {
            log.error("个税单月试算失败", e);
            return error();
        }
    }

    @GetMapping("/rules/{ruleVersion}")
    public Response<TaxRuleResponseDTO> queryTaxRule(@PathVariable String ruleVersion) {
        try {
            TaxRule taxRule = iitCalculateService.queryTaxRule(ruleVersion);
            return success(toTaxRuleResponseDTO(taxRule));
        } catch (IllegalArgumentException e) {
            log.warn("查询个税规则参数错误：{}", e.getMessage());
            return illegalParameter(e.getMessage());
        } catch (Exception e) {
            log.error("查询个税规则失败", e);
            return error();
        }
    }

    private MonthlySalaryTaxCommand toCommand(MonthlySalaryTaxRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        return MonthlySalaryTaxCommand.builder()
                .taxYear(requestDTO.getTaxYear())
                .month(requestDTO.getMonth())
                .ruleVersion(requestDTO.getRuleVersion())
                .grossIncome(requestDTO.getGrossIncome())
                .taxFreeIncome(requestDTO.getTaxFreeIncome())
                .specialDeduction(toSpecialDeduction(requestDTO.getSpecialDeduction()))
                .additionalDeduction(toAdditionalDeduction(requestDTO.getAdditionalDeduction()))
                .otherDeduction(requestDTO.getOtherDeduction())
                .accumulatedBeforeCurrentMonth(toAccumulatedTaxData(requestDTO.getAccumulatedBeforeCurrentMonth()))
                .build();
    }

    private SpecialDeduction toSpecialDeduction(SpecialDeductionDTO dto) {
        if (dto == null) {
            return null;
        }
        return SpecialDeduction.builder()
                .pension(dto.getPension())
                .medical(dto.getMedical())
                .unemployment(dto.getUnemployment())
                .housingFund(dto.getHousingFund())
                .build();
    }

    private AdditionalDeduction toAdditionalDeduction(AdditionalDeductionDTO dto) {
        if (dto == null) {
            return null;
        }
        return AdditionalDeduction.builder()
                .childrenEducation(dto.getChildrenEducation())
                .continuingEducation(dto.getContinuingEducation())
                .housingLoanInterest(dto.getHousingLoanInterest())
                .housingRent(dto.getHousingRent())
                .elderlyCare(dto.getElderlyCare())
                .infantCare(dto.getInfantCare())
                .seriousIllness(dto.getSeriousIllness())
                .build();
    }

    private AccumulatedTaxData toAccumulatedTaxData(AccumulatedTaxDataDTO dto) {
        if (dto == null) {
            return null;
        }
        return AccumulatedTaxData.builder()
                .grossIncome(dto.getGrossIncome())
                .taxFreeIncome(dto.getTaxFreeIncome())
                .specialDeduction(dto.getSpecialDeduction())
                .additionalDeduction(dto.getAdditionalDeduction())
                .otherDeduction(dto.getOtherDeduction())
                .paidTax(dto.getPaidTax())
                .build();
    }

    private TaxRuleResponseDTO toTaxRuleResponseDTO(TaxRule taxRule) {
        return TaxRuleResponseDTO.builder()
                .ruleVersion(taxRule.getRuleVersion())
                .countryOrRegion(taxRule.getCountryOrRegion())
                .effectiveFrom(taxRule.getEffectiveFrom())
                .effectiveTo(taxRule.getEffectiveTo())
                .basicDeductionMonthly(taxRule.getBasicDeductionMonthly())
                .basicDeductionAnnual(taxRule.getBasicDeductionAnnual())
                .annualTaxBrackets(toTaxBracketDTOList(taxRule.getAnnualTaxBrackets()))
                .monthlyTaxBrackets(toTaxBracketDTOList(taxRule.getMonthlyTaxBrackets()))
                .build();
    }

    private List<TaxBracketDTO> toTaxBracketDTOList(List<TaxBracket> taxBrackets) {
        return taxBrackets.stream()
                .map(taxBracket -> TaxBracketDTO.builder()
                        .level(taxBracket.getLevel())
                        .minExclusive(taxBracket.getMinExclusive())
                        .maxInclusive(taxBracket.getMaxInclusive())
                        .taxRate(taxBracket.getTaxRate())
                        .quickDeduction(taxBracket.getQuickDeduction())
                        .build())
                .collect(Collectors.toList());
    }

    private <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    private <T> Response<T> illegalParameter(String info) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(info)
                .build();
    }

    private <T> Response<T> error() {
        return Response.<T>builder()
                .code(Constants.ResponseCode.UN_ERROR.getCode())
                .info(Constants.ResponseCode.UN_ERROR.getInfo())
                .build();
    }

}
