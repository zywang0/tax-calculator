package org.study.trigger.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaxControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TaxController()).build();
    }

    @Test
    public void testCalculateMonthlySalaryTaxSuccess() throws Exception {
        mockMvc.perform(post("/api/tax/iit/salary/monthly/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"taxYear\": 2026,\n" +
                                "  \"month\": 7,\n" +
                                "  \"ruleVersion\": \"CN_IIT_2026\",\n" +
                                "  \"grossIncome\": 30000.00,\n" +
                                "  \"taxFreeIncome\": 0.00,\n" +
                                "  \"specialDeduction\": {\n" +
                                "    \"pension\": 2400.00,\n" +
                                "    \"medical\": 600.00,\n" +
                                "    \"unemployment\": 150.00,\n" +
                                "    \"housingFund\": 3600.00\n" +
                                "  },\n" +
                                "  \"additionalDeduction\": {\n" +
                                "    \"childrenEducation\": 2000.00,\n" +
                                "    \"housingLoanInterest\": 1000.00,\n" +
                                "    \"elderlyCare\": 3000.00\n" +
                                "  },\n" +
                                "  \"accumulatedBeforeCurrentMonth\": {\n" +
                                "    \"grossIncome\": 180000.00,\n" +
                                "    \"specialDeduction\": 40500.00,\n" +
                                "    \"additionalDeduction\": 36000.00,\n" +
                                "    \"paidTax\": 4000.00\n" +
                                "  }\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0000")))
                .andExpect(jsonPath("$.info", is("成功")))
                .andExpect(jsonPath("$.data.ruleVersion", is("CN_IIT_2026")))
                .andExpect(jsonPath("$.data.taxableIncome", is(85750.0)))
                .andExpect(jsonPath("$.data.taxRate", is(0.10)))
                .andExpect(jsonPath("$.data.quickDeduction", is(2520.0)))
                .andExpect(jsonPath("$.data.currentTaxPayable", is(2055.0)))
                .andExpect(jsonPath("$.data.netIncome", is(21195.0)));
    }

    @Test
    public void testCalculateMonthlySalaryTaxWhenTaxableIncomeIsZero() throws Exception {
        mockMvc.perform(post("/api/tax/iit/salary/monthly/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"taxYear\": 2026,\n" +
                                "  \"month\": 1,\n" +
                                "  \"grossIncome\": 5000.00\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0000")))
                .andExpect(jsonPath("$.data.taxableIncome", is(0.0)))
                .andExpect(jsonPath("$.data.currentTaxPayable", is(0.0)))
                .andExpect(jsonPath("$.data.netIncome", is(5000.0)));
    }

    @Test
    public void testCalculateMonthlySalaryTaxWhenMonthIsInvalid() throws Exception {
        mockMvc.perform(post("/api/tax/iit/salary/monthly/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"taxYear\": 2026,\n" +
                                "  \"month\": 13,\n" +
                                "  \"grossIncome\": 10000.00\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0002")))
                .andExpect(jsonPath("$.info", is("月份范围必须为 1 到 12")));
    }

    @Test
    public void testCalculateMonthlySalaryTaxWhenAmountIsNegative() throws Exception {
        mockMvc.perform(post("/api/tax/iit/salary/monthly/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"taxYear\": 2026,\n" +
                                "  \"month\": 1,\n" +
                                "  \"grossIncome\": -1.00\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0002")))
                .andExpect(jsonPath("$.info", is("税前收入不能为负数")));
    }

    @Test
    public void testQueryTaxRuleSuccess() throws Exception {
        mockMvc.perform(get("/api/tax/iit/rules/CN_IIT_2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0000")))
                .andExpect(jsonPath("$.data.ruleVersion", is("CN_IIT_2026")))
                .andExpect(jsonPath("$.data.basicDeductionMonthly", is(5000.0)))
                .andExpect(jsonPath("$.data.annualTaxBrackets", hasSize(7)))
                .andExpect(jsonPath("$.data.monthlyTaxBrackets", hasSize(7)));
    }

    @Test
    public void testQueryTaxRuleWhenRuleVersionIsUnknown() throws Exception {
        mockMvc.perform(get("/api/tax/iit/rules/UNKNOWN_RULE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0002")))
                .andExpect(jsonPath("$.info", is("税率规则版本不存在")));
    }

}
