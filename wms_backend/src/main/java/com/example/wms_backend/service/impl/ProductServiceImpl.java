package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.ProductQueryDTO;
import com.example.wms_backend.entity.Product;
import com.example.wms_backend.mapper.ProductMapper;
import com.example.wms_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Override
    public Product createProduct(Product product){
        // ===== 第1步：设置默认值 =====
        // 如果前端没有传状态，默认设置为上架（1）
        // 为什么要判断 null？因为前端可能传了 status=0（下架）
        // 如果不判断，每次都会把 status 改成 1，前端就没办法下架了
        if (product.getStatus() == null) {
            product.setStatus(1);
        }

        // ===== 第2步：设置默认预警阈值 =====
        // 如果前端没有传预警阈值，默认设置为10
        // 预警阈值的意思是：库存低于10个时，系统要提醒"该补货了"
        if (product.getWarningQty() == null) {
            product.setWarningQty(10);
        }

        // ===== 第3步：检查商品编码是否重复 =====
        // 商品编码（code）是唯一的，就像人的身份证号
        // 如果两个商品编码一样，系统就分不清谁是谁了
        // 所以新增前必须检查：这个编码有没有被其他商品使用
        Product existing = productMapper.findByCode(product.getCode());
        // 如果查到了，说明编码已经被占用了
        if (existing != null) {
            // throw 是"抛出异常"的意思
            // RuntimeException 是"运行时异常"
            // 抛出异常后，程序会立即停止执行，返回给全局异常处理器
            // 全局异常处理器会把这个信息包装成 JSON 返回给前端
            throw new RuntimeException("商品编码已存在：" + product.getCode());
        }

        // ===== 第4步：插入数据库 =====
        // 调用 Mapper 的 insert 方法
        // MyBatis 会执行 ProductMapper.xml 里的 INSERT 语句
        // 插入成功后，product.getId() 会被自动填上自增的ID值
        // 这是 XML 里 useGeneratedKeys="true" 的作用
        productMapper.insert(product);

        // ===== 第5步：返回新增的商品 =====
        // 返回的 product 对象已经包含了自增的 id 和默认的 createTime
        // 前端拿到这个返回值，就能知道"新增成功，商品的ID是xxx"
        return product;
    }

    @Override
    public Product updateProduct(Long id,Product product){
        Product existing = productMapper.findById(id);
        if(existing==null){
            throw new RuntimeException("商品不存在，ID:"+id);

        }
        if(product.getCode()!=null && !product.getCode().equals(existing.getCode())){
            Product codeCheck = productMapper.findByCode(product.getCode());
            if(codeCheck!=null){
                throw new RuntimeException("商品编码已存在："+product.getCode());
            }
        }
        product.setId(id);
        productMapper.update(product);
        return productMapper.findById(id);
    }

    @Override
    public void deleteProduct(Long id){
        Product existing = productMapper.findById(id);
        if(existing == null){
            throw new RuntimeException("商品不存在，ID:"+ id);
        }
        productMapper.deleteById(id);
    }

    @Override
    public Product getProductById(Long id){
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在，ID：" + id);
        }

        // ===== 返回结果 =====
        return product;
    }

    @Override
    public List<Product> getAllProducts(){
        return productMapper.findAll();
    }

    @Override
    public Object[] queryProducts(ProductQueryDTO queryDTO) {
        // ===== 第1步：查询当前页的数据 =====
        // 调用 Mapper 的条件查询方法
        // 传入了4个参数：
        // 1. name：商品名称（支持模糊查询）
        // 2. categoryId：分类ID（精确筛选）
        // 3. status：状态筛选（0或1）
        // 4. offset：分页起始位置（从第几条开始）
        // 5. pageSize：每页多少条
        //
        // SQL 执行结果：返回当前页的商品列表
        List<Product> list = productMapper.findByCondition(
                queryDTO.getName(),       // 名称
                queryDTO.getCategoryId(), // 分类ID
                queryDTO.getStatus(),     // 状态
                queryDTO.getOffset(),     // 起始位置
                queryDTO.getPageSize()    // 每页条数
        );

        // ===== 第2步：查询总记录数 =====
        // 分页查询需要知道"总共有多少条数据"
        // 前端根据这个总数计算"总共有多少页"
        // 比如：总共 100 条，每页 10 条 → 总共 10 页
        long total = productMapper.countByCondition(
                queryDTO.getName(),
                queryDTO.getCategoryId(),
                queryDTO.getStatus()
        );

        // ===== 第3步：组装返回结果 =====
        // 返回一个 Object[] 数组
        // [0] = 商品列表（List<Product>）
        // [1] = 总记录数（long）
        //
        // 为什么用 Object[] 而不是专门的类？
        // 因为这里暂时不想创建新的类
        // 后面等 Controller 写好了，我们会用 Map 来包装
        return new Object[]{list, total};
    }
}
