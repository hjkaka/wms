// ===== 第1行：包声明 =====
// 接口在 mapper 包里
package com.example.wms_backend.mapper;

// ===== 第2-6行：导入需要的类 =====
// 导入 Warehouse 实体类（CRUD 操作的对象）
import com.example.wms_backend.entity.Warehouse;
// 导入 MyBatis 的 @Mapper 注解
import org.apache.ibatis.annotations.Mapper;
// 导入 MyBatis 的 @Param 注解（给参数命名）
import org.apache.ibatis.annotations.Param;
// 导入 List 接口（用于返回多条记录）
import java.util.List;

// ===== 第7行：@Mapper 注解 =====
// 告诉 MyBatis：这是一个 Mapper 接口
// MyBatis 会自动扫描这个接口，并创建实现类
// Spring Boot 启动时会自动找到对应的 XML 映射文件
// 如果没有这个注解，Mapper 不会被扫描到
@Mapper
// ===== 第8行：接口声明 =====
// public = 公共的
// interface = 定义一个接口（只有方法声明，没有实现）
// WarehouseMapper = 接口名，通常是 实体名 + Mapper
// 接口的作用：定义"仓库管理"需要哪些数据库操作方法
// 具体的 SQL 写在对应的 XML 文件里
public interface WarehouseMapper {

    // ===== 方法1：根据ID查询仓库 =====
    // 这是一个查询方法，根据主键 ID 查一条记录
    // 
    // @Param("id")：给参数起名字，这样在 XML 里可以用 #{id} 引用
    // 如果不加 @Param，XML 里要用 #{param1}（不太直观）
    //
    // 返回值 Warehouse：查询到的仓库对象
    // 如果没查到，返回 null
    Warehouse findById(@Param("id") Long id);

    // ===== 方法2：根据编码查询仓库 =====
    // 仓库编码是唯一的，所以可以用来查询
    // 比如前端传过来 "GZ001"，我们查这个编码对应的仓库
    Warehouse findByCode(@Param("code") String code);

    // ===== 方法3：查询所有仓库 =====
    // 返回仓库列表（不分页，查所有）
    // 用于下拉选择框、列表页等场景
    List<Warehouse> findAll();

    // ===== 方法4：条件查询仓库（分页用）=====
    // 根据条件查询仓库列表，支持分页
    // 这里的条件：name（仓库名称，模糊查询）、status（状态筛选）
    // offset：从第几条开始查（分页用）
    // pageSize：每页查几条（分页用）
    //
    // 返回值：符合条件的仓库列表
    List<Warehouse> findByCondition(
            @Param("name") String name,           // 仓库名称（模糊查询）
            @Param("status") Integer status,      // 状态筛选
            @Param("offset") Integer offset,      // 分页起始位置
            @Param("pageSize") Integer pageSize   // 每页条数
    );

    // ===== 方法5：统计符合条件的仓库数量 =====
    // 和方法4的查询条件一样，但返回的是数量（用于分页）
    // 比如前端显示"共 100 条，每页 10 条"，就需要这个总数
    long countByCondition(
            @Param("name") String name,
            @Param("status") Integer status
    );

    // ===== 方法6：新增仓库 =====
    // 插入一条新的仓库记录
    //
    // @Param("warehouse")：给参数起个名字
    // 方法接收一个 Warehouse 对象，里面包含要插入的数据
    Warehouse insert(@Param("warehouse") Warehouse warehouse);

    // ===== 方法7：修改仓库 =====
    // 根据 ID 更新仓库信息
    // warehouse 对象里包含修改后的数据
    int update(@Param("warehouse") Warehouse warehouse);

    // ===== 方法8：删除仓库 =====
    // 根据 ID 删除仓库
    // 返回值 int：影响的行数（成功删除返回 1，没找到返回 0）
    int deleteById(@Param("id") Long id);

    // ===== 接口的结束括号 =====
}