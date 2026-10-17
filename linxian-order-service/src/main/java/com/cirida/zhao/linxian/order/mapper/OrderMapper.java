package com.cirida.zhao.linxian.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cirida.zhao.linxian.order.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 状态机 CAS 条件更新：仅当 status = current 时更新为 target，防止并发重复流转。
     * payment_time / cancel_time 传 null 时保持不变（COALESCE）。
     */
    @Update("UPDATE orders SET status = #{target}, " +
            "payment_time = COALESCE(#{paymentTime}, payment_time), " +
            "cancel_time = COALESCE(#{cancelTime}, cancel_time) " +
            "WHERE id = #{id} AND status = #{current}")
    int casStatus(@Param("id") Long id,
                  @Param("current") Integer current,
                  @Param("target") Integer target,
                  @Param("paymentTime") LocalDateTime paymentTime,
                  @Param("cancelTime") LocalDateTime cancelTime);
}
