package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.ProductQueryDTO;
import com.example.wms_backend.entity.Product;
import com.example.wms_backend.mapper.ProductMapper;
import com.example.wms_backend.service.CategoryService;
import com.example.wms_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryService categoryService;
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
        // ===== 分类过滤展开 =====
        // 若指定了分类，需把它及其所有子分类都纳入过滤范围（选父分类时能看到其下子分类的商品）。
        // 例如选顶级"数码产品"，则手机/电脑等子分类下的商品都要查到。
        List<Long> categoryIds = null;
        if (queryDTO.getCategoryId() != null) {
            categoryIds = categoryService.collectRelatedIds(queryDTO.getCategoryId());
        }

        List<Product> list;
        long total;
        if (categoryIds != null) {
            // 展开后的分类 id 集合非空才查（分类若不存在则查到空）
            list = productMapper.findByConditionIds(
                    queryDTO.getName(), categoryIds, queryDTO.getStatus(),
                    queryDTO.getOffset(), queryDTO.getPageSize());
            total = productMapper.countByConditionIds(
                    queryDTO.getName(), categoryIds, queryDTO.getStatus());
        } else {
            list = productMapper.findByCondition(
                    queryDTO.getName(), queryDTO.getCategoryId(), queryDTO.getStatus(),
                    queryDTO.getOffset(), queryDTO.getPageSize());
            total = productMapper.countByCondition(
                    queryDTO.getName(), queryDTO.getCategoryId(), queryDTO.getStatus());
        }

        // ===== 组装返回结果 =====
        return new Object[]{list, total};
    }
}
