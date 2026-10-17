package com.cirida.zhao.linxian.inventory.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存流水类型。
 */
@Getter
@AllArgsConstructor
public enum StockDeductType {

    PRE_DEDUCT(1, "预扣"),
    ROLLBACK(2, "回补"),
    CONFIRM(3, "确认");

    private final int code;
    private final String desc;
}
