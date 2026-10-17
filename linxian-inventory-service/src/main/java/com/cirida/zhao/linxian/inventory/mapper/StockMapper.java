package com.cirida.zhao.linxian.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cirida.zhao.linxian.inventory.entity.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 预扣：available 减、locked 加。条件更新 available >= qty 作为 DB 层防超卖双保险，
     * version + 1 记录并发修改次数（乐观锁）。
     */
    @Update("UPDATE stock SET available = available - #{qty}, locked = locked + #{qty}, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND available >= #{qty}")
    int deductAvailable(@Param("skuId") Long skuId, @Param("qty") Integer qty);

    /**
     * 回补：available 加、locked 减。
     */
    @Update("UPDATE stock SET available = available + #{qty}, locked = locked - #{qty}, version = version + 1 " +
            "WHERE sku_id = #{skuId}")
    int restoreAvailable(@Param("skuId") Long skuId, @Param("qty") Integer qty);

    /**
     * 确认占用：locked 减、total 减（商品真正出库）。条件更新 locked >= qty。
     */
    @Update("UPDATE stock SET locked = locked - #{qty}, total = total - #{qty}, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND locked >= #{qty}")
    int confirmLocked(@Param("skuId") Long skuId, @Param("qty") Integer qty);
}
