# 个税所得税计算器后端接口设计

## 1. 目标范围

一期优先实现中国大陆居民个人综合所得中的工资薪金个税计算，暂不考虑前端页面。

核心目标：

- 支持按月工资薪金预扣预缴计算。
- 支持年度累计预扣法计算，并返回每月明细。
- 支持年终奖单独计税测算。
- 支持税率表、专项附加扣除标准、社保公积金上限等规则版本查询。
- 输出清晰的应纳税所得额、适用税率、速算扣除数、应纳税额、已缴税额、本期应缴税额和税后收入。

暂不纳入一期：

- 年度汇算清缴退补税申报全流程。
- 非居民个人计税。
- 经营所得、财产租赁、财产转让、利息股息红利、偶然所得等非工资类所得。
- 多城市社保公积金基数政策自动查询。
- 用户账户、历史记录、发票或税务申报对接。

## 2. 税务规则抽象

个税规则不要硬编码在 Controller 中，建议在领域层抽象为 `TaxRule`。

需要支持的规则项：

- 基本减除费用：默认 5000 元/月，60000 元/年。
- 综合所得年度税率表：7 级超额累进税率。
- 月度换算税率表：用于年终奖单独计税等场景。
- 专项扣除：养老保险、医疗保险、失业保险、住房公积金等。
- 专项附加扣除：子女教育、继续教育、大病医疗、住房贷款利息、住房租金、赡养老人、3 岁以下婴幼儿照护。
- 其他扣除：企业年金、商业健康险、税延养老保险、依法确定的其他扣除。
- 规则版本：例如 `CN_IIT_2026`，后续政策变化时新增版本，不破坏旧计算。

## 3. 功能清单

### 3.1 单月工资个税试算

输入某个月的工资收入、社保公积金、专项附加扣除、其他扣除、已累计收入和已累计扣除，计算本月应预扣预缴税额。

必须返回：

- 本月税前收入。
- 本月免税收入。
- 本月专项扣除。
- 本月专项附加扣除。
- 本月其他扣除。
- 累计收入。
- 累计减除费用。
- 累计专项扣除。
- 累计专项附加扣除。
- 累计其他扣除。
- 累计应纳税所得额。
- 适用税率。
- 速算扣除数。
- 累计应纳税额。
- 累计已缴税额。
- 本月应缴税额。
- 本月税后收入。

### 3.2 年度工资个税试算

输入 1 到 12 个月的工资与扣除数据，逐月按累计预扣法计算。

必须返回：

- 年度汇总：全年税前收入、全年扣除、全年应纳税所得额、全年应纳税额、全年税后收入。
- 月度明细：每月的应纳税所得额、税率、应缴税额和税后收入。
- 可选诊断信息：是否存在负数收入、扣除大于收入、月份缺失等。

### 3.3 年终奖单独计税试算

输入全年一次性奖金金额，按奖金除以 12 后匹配月度换算税率表，计算应纳税额。

必须返回：

- 奖金金额。
- 月均奖金。
- 适用税率。
- 速算扣除数。
- 应纳税额。
- 税后奖金。

### 3.4 综合测算

输入年度工资和年终奖，同时输出两种方案：

- 年终奖单独计税。
- 年终奖并入综合所得。

必须返回：

- 两种方案的年度税额。
- 两种方案的税后收入。
- 推荐方案。
- 差额。

### 3.5 规则查询

查询当前支持的计税规则。

必须返回：

- 支持的规则版本列表。
- 默认规则版本。
- 税率表。
- 基本减除费用。
- 专项附加扣除标准。
- 规则生效日期。

### 3.6 参数校验

必须校验：

- 金额不能为负数，退薪等特殊场景后续单独建模。
- 月份范围为 1 到 12。
- 年份范围合理，例如 2019 到当前年份。
- 税率规则版本必须存在。
- 同一年度试算中月份不能重复。
- BigDecimal 金额统一保留 2 位小数。

## 4. 功能优先级评估

优先级判断维度：

- 用户价值：是否能直接完成一次个税试算。
- 依赖关系：是否是其他功能的前置能力。
- 实现复杂度：是否容易先验证公式、边界和数据结构。
- 政策风险：规则是否容易变化，是否需要版本化隔离。
- 测试成本：是否容易通过单元测试覆盖关键边界。

### 4.1 P0 最小可用版本

P0 目标是先做出一个可信、可测、可通过 HTTP 调用的工资个税计算核心。

必须实现：

- 税率表和值对象：`TaxRule`、`TaxBracket`。
- 内存版规则仓储：先支持一个默认规则版本，例如 `CN_IIT_2026`。
- 金额、月份、规则版本基础校验。
- 单月工资个税试算接口：`POST /api/tax/iit/salary/monthly/calculate`。
- 规则详情查询接口：`GET /api/tax/iit/rules/{ruleVersion}`。
- 单月计算单元测试：覆盖 0、档位边界、跨档、扣除大于收入。

暂缓实现：

- 年度 12 个月完整测算。
- 年终奖。
- 年终奖方案比较。
- 社保公积金自动基数计算。
- 用户历史记录和持久化规则配置。

交付标准：

- Controller 可以返回统一 `Response<T>`。
- 所有金额字段使用 `BigDecimal`。
- 税率匹配和速算扣除数选择有单元测试。
- 单月接口可独立验证，不依赖数据库和外部服务。

### 4.2 P1 完整工资测算版本

P1 在 P0 的单月能力上，补齐年度累计预扣法，让计算器真正覆盖常见工资场景。

必须实现：

- 年度工资个税试算接口：`POST /api/tax/iit/salary/annual/calculate`。
- 逐月累计计算逻辑。
- 年度汇总结果。
- 月度明细结果。
- 规则版本列表接口：`GET /api/tax/iit/rules`。
- 年度计算单元测试：覆盖连续 12 个月、月份缺失、月份重复、年度跨档。

暂缓实现：

- 年终奖并入综合所得与单独计税对比。
- 规则持久化管理。
- 城市社保公积金政策自动计算。

交付标准：

- 年度接口可基于 1 到 12 个月输入稳定计算。
- 月度明细中的累计税额、本月税额、税后收入可追踪。
- 单月和年度计算复用同一套领域模型与税率匹配逻辑。

### 4.3 P2 年终奖和方案比较版本

P2 增加年终奖相关能力，适合在工资计算稳定后实现。

必须实现：

- 年终奖单独计税接口：`POST /api/tax/iit/bonus/calculate`。
- 年终奖月度换算税率表匹配。
- 年终奖方案比较接口：`POST /api/tax/iit/annual-bonus/compare`。
- 推荐方案输出：`SEPARATE_BONUS` 或 `MERGED_INCOME`。
- 年终奖边界测试：月均奖金等于 3000、12000、25000 等档位边界。

交付标准：

- 单独计税结果和并入综合所得结果可同时返回。
- 推荐方案只基于税后收入或税额差额，避免掺入主观解释。
- 方案比较复用 P1 年度工资计算能力。

### 4.4 P3 扩展能力

P3 属于增强能力，不影响个税计算器核心闭环。

可选实现：

- 规则配置持久化：将税率表、扣除标准放入数据库。
- 多规则版本切换：支持历史年度规则回放。
- 城市社保公积金基数和比例配置。
- 计算过程解释接口：返回每一步公式和中间值。
- 计算结果保存与查询。
- 非居民个人、劳务报酬、稿酬、特许权使用费等扩展所得类型。

### 4.5 推荐当前开发顺序

建议当前项目按以下顺序推进：

1. P0-1：创建 `domain/tax` 包、规则和值对象。
2. P0-2：实现税率匹配、累计预扣公式和单月计算服务。
3. P0-3：实现单月 HTTP 接口和规则详情接口。
4. P0-4：补单元测试，确认边界金额正确。
5. P1-1：实现年度逐月累计计算。
6. P1-2：实现年度 HTTP 接口和规则列表接口。
7. P2-1：实现年终奖单独计税。
8. P2-2：实现年终奖方案比较。
9. P3：按实际使用反馈选择扩展。

当前最建议优先启动 `P0-1` 到 `P0-4`。这部分范围小、依赖少、最容易验证准确性，能最快形成可用后端闭环。

## 5. API 设计

项目已有统一响应结构 `Response<T>`，接口继续使用：

```json
{
  "code": "0000",
  "info": "成功",
  "data": {}
}
```

### 5.1 单月工资个税试算

`POST /api/tax/iit/salary/monthly/calculate`

请求：

```json
{
  "taxYear": 2026,
  "month": 7,
  "ruleVersion": "CN_IIT_2026",
  "grossIncome": 30000.00,
  "taxFreeIncome": 0.00,
  "specialDeduction": {
    "pension": 2400.00,
    "medical": 600.00,
    "unemployment": 150.00,
    "housingFund": 3600.00
  },
  "additionalDeduction": {
    "childrenEducation": 2000.00,
    "continuingEducation": 0.00,
    "housingLoanInterest": 1000.00,
    "housingRent": 0.00,
    "elderlyCare": 3000.00,
    "infantCare": 0.00,
    "seriousIllness": 0.00
  },
  "otherDeduction": 0.00,
  "accumulatedBeforeCurrentMonth": {
    "grossIncome": 180000.00,
    "taxFreeIncome": 0.00,
    "specialDeduction": 40500.00,
    "additionalDeduction": 36000.00,
    "otherDeduction": 0.00,
    "paidTax": 6480.00
  }
}
```

响应：

```json
{
  "taxYear": 2026,
  "month": 7,
  "ruleVersion": "CN_IIT_2026",
  "taxableIncome": 104850.00,
  "taxRate": 0.10,
  "quickDeduction": 2520.00,
  "accumulatedTaxPayable": 7965.00,
  "paidTaxBeforeCurrentMonth": 6480.00,
  "currentTaxPayable": 1485.00,
  "netIncome": 18765.00,
  "formula": "currentTaxPayable = accumulatedTaxPayable - paidTaxBeforeCurrentMonth"
}
```

### 5.2 年度工资个税试算

`POST /api/tax/iit/salary/annual/calculate`

请求：

```json
{
  "taxYear": 2026,
  "ruleVersion": "CN_IIT_2026",
  "months": [
    {
      "month": 1,
      "grossIncome": 30000.00,
      "taxFreeIncome": 0.00,
      "specialDeduction": {
        "pension": 2400.00,
        "medical": 600.00,
        "unemployment": 150.00,
        "housingFund": 3600.00
      },
      "additionalDeduction": {
        "childrenEducation": 2000.00,
        "continuingEducation": 0.00,
        "housingLoanInterest": 1000.00,
        "housingRent": 0.00,
        "elderlyCare": 3000.00,
        "infantCare": 0.00,
        "seriousIllness": 0.00
      },
      "otherDeduction": 0.00
    }
  ]
}
```

响应：

```json
{
  "taxYear": 2026,
  "ruleVersion": "CN_IIT_2026",
  "summary": {
    "grossIncome": 360000.00,
    "totalDeduction": 153000.00,
    "taxableIncome": 147000.00,
    "taxPayable": 15480.00,
    "netIncome": 191520.00
  },
  "monthlyItems": [
    {
      "month": 1,
      "taxableIncome": 18250.00,
      "taxRate": 0.03,
      "quickDeduction": 0.00,
      "currentTaxPayable": 547.50,
      "netIncome": 22702.50
    }
  ]
}
```

### 5.3 年终奖单独计税试算

`POST /api/tax/iit/bonus/calculate`

请求：

```json
{
  "taxYear": 2026,
  "ruleVersion": "CN_IIT_2026",
  "bonus": 60000.00
}
```

响应：

```json
{
  "bonus": 60000.00,
  "averageMonthlyBonus": 5000.00,
  "taxRate": 0.10,
  "quickDeduction": 210.00,
  "taxPayable": 5790.00,
  "netBonus": 54210.00
}
```

### 5.4 年终奖方案比较

`POST /api/tax/iit/annual-bonus/compare`

请求：

```json
{
  "taxYear": 2026,
  "ruleVersion": "CN_IIT_2026",
  "salaryMonths": [],
  "bonus": 60000.00
}
```

响应：

```json
{
  "separateBonusPlan": {
    "taxPayable": 21270.00,
    "netIncome": 245730.00
  },
  "mergedBonusPlan": {
    "taxPayable": 23880.00,
    "netIncome": 243120.00
  },
  "recommendedPlan": "SEPARATE_BONUS",
  "taxSaving": 2610.00
}
```

### 5.5 查询规则版本

`GET /api/tax/iit/rules`

响应：

```json
{
  "defaultRuleVersion": "CN_IIT_2026",
  "rules": [
    {
      "ruleVersion": "CN_IIT_2026",
      "countryOrRegion": "CN",
      "effectiveFrom": "2026-01-01",
      "effectiveTo": "2026-12-31"
    }
  ]
}
```

### 5.6 查询规则详情

`GET /api/tax/iit/rules/{ruleVersion}`

响应：

```json
{
  "ruleVersion": "CN_IIT_2026",
  "basicDeductionMonthly": 5000.00,
  "basicDeductionAnnual": 60000.00,
  "annualTaxBrackets": [
    {
      "level": 1,
      "minExclusive": 0.00,
      "maxInclusive": 36000.00,
      "taxRate": 0.03,
      "quickDeduction": 0.00
    }
  ],
  "monthlyTaxBrackets": [
    {
      "level": 1,
      "minExclusive": 0.00,
      "maxInclusive": 3000.00,
      "taxRate": 0.03,
      "quickDeduction": 0.00
    }
  ],
  "additionalDeductionStandards": []
}
```

## 6. 领域模型建议

建议新增包：

- `test-domain/src/main/java/org/study/domain/tax/model/entity`
- `test-domain/src/main/java/org/study/domain/tax/model/valobj`
- `test-domain/src/main/java/org/study/domain/tax/service`
- `test-trigger/src/main/java/org/study/trigger/http`

核心类：

- `IitCalculateService`：个税计算领域服务接口。
- `IitCalculateServiceImpl`：计算实现。
- `TaxRuleRepository`：规则查询接口，一期可用内存实现。
- `TaxRule`：税务规则聚合。
- `TaxBracket`：税率档位值对象。
- `SalaryMonthIncome`：月度工资收入实体。
- `SpecialDeduction`：专项扣除值对象。
- `AdditionalDeduction`：专项附加扣除值对象。
- `MonthlyTaxResult`：单月计算结果。
- `AnnualTaxResult`：年度计算结果。
- `BonusTaxResult`：年终奖结果。

HTTP 层 DTO：

- `MonthlySalaryTaxRequestDTO`
- `AnnualSalaryTaxRequestDTO`
- `BonusTaxRequestDTO`
- `AnnualBonusCompareRequestDTO`
- `TaxRuleResponseDTO`
- `MonthlyTaxResultDTO`
- `AnnualTaxResultDTO`
- `BonusTaxResultDTO`
- `AnnualBonusCompareResultDTO`

## 7. 计算公式

### 7.1 累计预扣法

```text
累计预扣预缴应纳税所得额
= 累计收入
- 累计免税收入
- 累计减除费用
- 累计专项扣除
- 累计专项附加扣除
- 累计依法确定的其他扣除

累计应预扣预缴税额
= 累计预扣预缴应纳税所得额 * 预扣率
- 速算扣除数

本期应预扣预缴税额
= 累计应预扣预缴税额
- 累计已预扣预缴税额
```

若累计应纳税所得额小于等于 0，则累计应纳税额和本期应缴税额都按 0 处理。

### 7.2 年终奖单独计税

```text
月均奖金 = 全年一次性奖金 / 12
应纳税额 = 全年一次性奖金 * 适用税率 - 速算扣除数
```

## 8. 推荐实现顺序

详细分期见「4. 功能优先级评估」。实现时遵循以下原则：

1. 先完成 P0 单月工资计算闭环，再扩展年度和年终奖。
2. 先落领域模型和计算服务，再暴露 HTTP Controller。
3. 所有计算功能都要先有单元测试覆盖关键边界，再继续叠加新接口。
4. 税率表、扣除标准和规则版本统一走 `TaxRuleRepository`，不要散落在接口层。
5. 金额统一使用 `BigDecimal`，避免使用 `double` 或 `float` 参与税额计算。

## 9. 测试用例建议

必须覆盖：

- 应纳税所得额为 0。
- 应纳税所得额刚好等于档位上限，例如 36000、144000。
- 应纳税所得额刚超过档位上限 0.01。
- 年度 12 个月连续累计计算。
- 年终奖月均值刚好等于 3000、12000、25000 等边界。
- 专项扣除和专项附加扣除均为 0。
- 扣除大于收入。
- 非法月份、负数金额、未知规则版本。
