// ===== 第1行：包声明 =====
package com.example.wms_backend.service;

// ===== 第2-6行：导入需要的类 =====
// ProductQueryDTO：接收前端传的分页查询参数
import com.example.wms_backend.dto.ProductQueryDTO;
// Product：商品实体类，对应数据库 product 表
import com.example.wms_backend.entity.Product;
// List：Java 的列表接口，用于返回多条记录
import java.util.List;

// ===== 第7行：接口声明 =====
/**
 * 商品管理 Service 接口
 *
 * 接口的作用：定义"商品管理有哪些业务功能"
 * 具体的实现写在 ProductServiceImpl 里
 *
 * 为什么要分接口和实现？
 * 1. 面向接口编程：调用方只依赖接口，不依赖实现
 * 2. 便于替换实现：比如以后要改缓存逻辑，换个实现类就行
 * 3. 便于测试：可以 mock 接口，不需要真实数据库
 */
public interface ProductService {

    /**
     * 新增商品
     *
     * 业务流程：
     * 1. 检查商品编码是否重复
     * 2. 设置默认值（如状态默认为上架）
     * 3. 保存到数据库
     * 4. 返回带自增ID的商品对象
     *
     * @param product 前端传来的商品信息（不含 id、createTime、updateTime）
     * @return 新增后的商品对象（包含自增的 id）
     */
    Product createProduct(Product product);

    /**
     * 修改商品
     *
     * 业务流程：
     * 1. 检查商品是否存在
     * 2. 如果修改了编码，检查新编码是否和其他商品冲突
     * 3. 更新数据库
     * 4. 返回更新后的商品信息
     *
     * @param id 要修改的商品ID
     * @param product 修改后的商品信息
     * @return 更新后的商品对象
     */
    Product updateProduct(Long id, Product product);

    /**
     * 删除商品
     *
     * 注意：这里是物理删除（真的从数据库删掉）
     * 实际项目中通常用"逻辑删除"（把 status 改成 -1）
     * 但我们先按物理删除实现
     *
     * @param id 要删除的商品ID
     */
    void deleteProduct(Long id);

    /**
     * 根据ID查询商品
     *
     * @param id 商品ID
     * @return 商品对象
     * @throws RuntimeException 如果商品不存在
     */
    Product getProductById(Long id);

    /**
     * 查询所有上架商品
     *
     * 用于前端下拉选择框、列表页等场景
     * 只返回 status=1 的商品
     *
     * @return 上架商品列表
     */
    List<Product> getAllProducts();

    /**
     * 条件分页查询商品
     *
     * 支持的条件：
     * - 名称模糊查询
     * - 按分类筛选
     * - 按状态筛选
     *
     * 返回值是一个 Object[] 数组：
     * - [0] = List<Product> 商品列表
     * - [1] = Long 符合条件的总记录数
     *
     * @param queryDTO 查询条件 + 分页参数
     * @return [列表, 总数]
     */
    Object[] queryProducts(ProductQueryDTO queryDTO);
}