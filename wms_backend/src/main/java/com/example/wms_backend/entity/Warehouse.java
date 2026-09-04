// ===== 第1行：包声明 =====
// 告诉 Java 这个类在哪个包里
// 包路径要和目录结构一致：entity/Warehouse.java → package ...entity
package com.example.wms_backend.entity;

// ===== 第2-4行：导入需要的类 =====
// lombok.Data 是 Lombok 注解，用来自动生成 getter/setter
import lombok.Data;
// java.time.LocalDateTime 是 Java 8+ 的日期时间类
import java.time.LocalDateTime;

// ===== 第5行：@Data 注解 =====
// 这是 Lombok 的注解，作用：
// 1. 自动为每个属性生成 getter 方法（如 getId(), getName()）
// 2. 自动为每个属性生成 setter 方法（如 setId(), setName()）
// 3. 自动生成 toString() 方法（方便打印调试）
// 4. 自动生成 equals() 和 hashCode() 方法
// 这样就不用手写几十个样板代码了
@Data
// ===== 第6行：类声明 =====
// public = 公共的，任何地方都能访问
// class = 定义一个类
// Warehouse = 类名，通常和数据库表名对应（表名 warehouse → 类名 Warehouse）
// 这个类对应数据库里的 warehouse 表
public class Warehouse {

    // ===== 第7行：主键 =====
    // id 字段：仓库的唯一标识，数据库自增
    // Long = 长整型（对应数据库 BIGINT）
    // 主键字段命名通常用 id
    private Long id;

    // ===== 第8行：仓库名称 =====
    // 如："广州主仓"、"深圳分仓"
    // String = 字符串类型（对应数据库 VARCHAR）
    private String name;

    // ===== 第9行：仓库编码 =====
    // 唯一标识仓库的编码，如："GZ001"、"SZ002"
    // 编码通常用于系统内部引用，比 id 更有可读性
    private String code;

    // ===== 第10行：仓库地址 =====
    // 详细地址，如："广州市天河区科技园路88号"
    private String address;

    // ===== 第11行：负责人ID =====
    // 关联 sys_user 表的 id 字段
    // 表示这个仓库由哪个员工负责
    // 命名用 managerId（小驼峰），对应数据库字段 manager_id（下划线）
    // MyBatis 会自动转换：manager_id → managerId
    private Long managerId;

    // ===== 第12行：状态 =====
    // 仓库的启用/停用状态
    // 0 = 停用
    // 1 = 启用
    // Integer = 整型（对应数据库 INT）
    private Integer status;

    // ===== 第13行：创建时间 =====
    // 记录仓库信息是什么时候创建的
    // LocalDateTime = Java 8+ 的日期时间类，包含日期和时间
    // 对应数据库 DATETIME 类型
    // 命名用 createTime，对应数据库 create_time
    private LocalDateTime createTime;

    // ===== 第14行：更新时间 =====
    // 记录仓库信息最后一次修改的时间
    // 每次修改仓库数据时，这个时间会自动更新
    private LocalDateTime updateTime;

    // ===== 类的结束括号 =====
    // 这对花括号包住了整个类的定义
    // 注意：这是类的最后一个 }，不要漏掉！
}