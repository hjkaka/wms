// ===== 第1行：包声明 =====
package com.example.wms_backend.mapper;

// ===== 第2-8行：导入需要的类 =====
import com.example.wms_backend.entity.StockInOrder;
import com.example.wms_backend.dto.StockInPageDTO;
import com.example.wms_backend.vo.StockInOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

// ===== 第9行：@Mapper 注解 =====
// 告诉 MyBatis：这是一个 Mapper 接口，需要创建实现类
@Mapper
// ===== 第10行：接口声明 =====
public interface StockInOrderMapper {

    // ===== 方法1：插入入库单 =====
    // @Param 给参数起名字，XML 里可以用 #{xxx} 引用
    // 返回 int：影响的行数
    int insert(@Param("order") StockInOrder order);

    // ===== 方法2：根据ID查询入库单 =====
    // 查询结果：返回 StockInOrder 对象
    StockInOrder findById(@Param("id") Long id);

    String getMaxOrderNoByDate(@Param("prefix") String prefix);

    /**
     * 更新单据状态（审核流用）
     * 走 status 状态机：0草稿/1待审/2已审/3已过账
     * audited 相关字段仅当外部显式传入时才覆盖（审核/驳回时写入审核人、时间）
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("auditorId") Long auditorId,
                     @Param("auditTime") LocalDateTime auditTime);

    // 分页查询入库单（联表仓库/经办人），dto 装查询条件，offset 是跳过条数
    List<StockInOrderVO> searchPage(@Param("dto") StockInPageDTO dto, @Param("offset") int offset);

    // 满足条件的总条数，用于分页返回 total
    long countSearch(@Param("dto") StockInPageDTO dto);
}