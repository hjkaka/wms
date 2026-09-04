package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.StockOutOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 出库单 Mapper 接口
 * 负责操作 stock_out_order 表
 * 注意：Mapper 接口只定义方法，SQL 写在对应的 XML 里
 */
@Mapper
public interface StockOutOrderMapper {

    // 插入出库单
    // 返回 int：影响的行数
    int insert(@Param("order") StockOutOrder order);

    // 根据ID查询出库单
    // 返回 StockOutOrder 对象
    StockOutOrder findById(@Param("id") Long id);

    // 查询今天最大的出库单号
    // 用于生成唯一且递增的单号（CK + 日期 + 3位流水号）
    String getMaxOrderNoByDate(@Param("prefix") String prefix);
}