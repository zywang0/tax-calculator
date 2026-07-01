package org.study.trigger.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TaxBracketDTO {

    private Integer level;

    private BigDecimal minExclusive;

    private BigDecimal maxInclusive;

    private BigDecimal taxRate;

    private BigDecimal quickDeduction;

}
