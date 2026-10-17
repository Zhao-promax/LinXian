package com.cirida.zhao.linxian.inventory.service;

import com.cirida.zhao.linxian.api.inventory.dto.PreDeductRequest;
import com.cirida.zhao.linxian.api.inventory.dto.StockOperateRequest;

public interface StockService {

    void preDeduct(PreDeductRequest request);

    void rollback(StockOperateRequest request);

    void confirm(StockOperateRequest request);
}
