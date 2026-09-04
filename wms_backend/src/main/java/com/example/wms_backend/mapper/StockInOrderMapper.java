// ===== 第1行：包声明 =====
package com.example.wms_backend.mapper;

// ===== 第2-8行：导入需要的类 =====
import com.example.wms_backend.entity.StockInOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}