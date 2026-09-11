package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.StockOutOrder;
import com.example.wms_backend.dto.StockOutPageDTO;
import com.example.wms_backend.vo.StockOutOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 更新单据状态（审核流用）
     * 状态机：0草稿/1待审/2已审/3已过账
     * audit 相关字段仅当外部显式传入时才覆盖
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("auditorId") Long auditorId,
                     @Param("auditTime") LocalDateTime auditTime);

    // 分页查询出库单（联表仓库/经办人）
    List<StockOutOrderVO> searchPage(@Param("dto") StockOutPageDTO dto, @Param("offset") int offset);

    // 满足条件的总条数，用于分页返回 total
    long countSearch(@Param("dto") StockOutPageDTO dto);
}