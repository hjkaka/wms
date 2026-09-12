package com.example.wms_backend.aspect;

import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.entity.*;
import com.example.wms_backend.mapper.AuditLogMapper;
import com.example.wms_backend.mapper.CategoryMapper;
import com.example.wms_backend.mapper.ProductMapper;
import com.example.wms_backend.mapper.StockInOrderMapper;
import com.example.wms_backend.mapper.StockOutOrderMapper;
import com.example.wms_backend.mapper.StockTakeOrderMapper;
import com.example.wms_backend.mapper.SysUserMapper;
import com.example.wms_backend.mapper.WarehouseMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作审计切面
 * 拦截标注了 @AuditLog 的写操作方法，记录"谁、何时、对哪个数据、做了什么改变"。
 * 关键点：修改/删除的"旧值"必须在 proceed() 之前读取，否则拿到的是已变更的数据。
 * 审计写入采用 best-effort：失败只记日志，不中断业务。
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private StockInOrderMapper stockInOrderMapper;
    @Autowired
    private StockOutOrderMapper stockOutOrderMapper;
    @Autowired
    private StockTakeOrderMapper stockTakeOrderMapper;

    @Around("@annotation(auditLog)")
    public Object record(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        Object[] args = pjp.getArgs();
        // 修改/删除：在方法执行前捕获"变更前"的旧记录
        Object old = loadBefore(auditLog, args);

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            throw t;
        }

        // 业务成功后构建并写入审计日志（best-effort）
        try {
            buildAndSave(auditLog, result, old);
        } catch (Exception e) {
            log.error("[audit] 写入审计日志失败: {}", e.getMessage(), e);
        }
        return result;
    }

    /** 在 proceed 之前，按 module:action 取回旧实体（仅 UPDATE/DELETE 需要） */
    private Object loadBefore(AuditLog auditLog, Object[] args) {
        String key = auditLog.module() + ":" + auditLog.action();
        try {
            if ("product:UPDATE".equals(key) || "product:DELETE".equals(key)) {
                Long id = (args != null && args.length > 0) ? (Long) args[0] : null;
                return id == null ? null : productMapper.findById(id);
            }
            if ("warehouse:UPDATE".equals(key) || "warehouse:DELETE".equals(key)) {
                Long id = (args != null && args.length > 0) ? (Long) args[0] : null;
                return id == null ? null : warehouseMapper.findById(id);
            }
            if ("category:UPDATE".equals(key) || "category:DELETE".equals(key)) {
                Long id = (args != null && args.length > 0) ? (Long) args[0] : null;
                return id == null ? null : categoryMapper.findById(id);
            }
            if ("stockin:REVERSE".equals(key) || "stockin:DELETE".equals(key)) {
                Long id = (args != null && args.length > 0) ? (Long) args[0] : null;
                return id == null ? null : stockInOrderMapper.findById(id);
            }
            if ("stockout:REVERSE".equals(key) || "stockout:DELETE".equals(key)) {
                Long id = (args != null && args.length > 0) ? (Long) args[0] : null;
                return id == null ? null : stockOutOrderMapper.findById(id);
            }
            if ("stocktake:POST".equals(key)) {
                Long id = (args != null && args.length > 0) ? (Long) args[0] : null;
                return id == null ? null : stockTakeOrderMapper.findById(id);
            }
        } catch (Exception e) {
            log.warn("[audit] 读取旧值失败: {}", e.getMessage());
        }
        return null;
    }

    private void buildAndSave(AuditLog auditLog, Object result, Object old) {
        // entity.AuditLog 与 annotation.AuditLog 同名，用全限定名区分
        com.example.wms_backend.entity.AuditLog ent = new com.example.wms_backend.entity.AuditLog();
        ent.setModule(auditLog.module());
        ent.setAction(auditLog.action());
        fillOperator(ent);
        fillRequest(ent);

        Object[] args = null; // 需要时才从 pjp 取
        switch (auditLog.module() + ":" + auditLog.action()) {
            case "product:CREATE":
                fillProductCreate(ent, unwrap(result));
                break;
            case "product:UPDATE":
                fillProductUpdate(ent, (Product) old, extractEntity(result, Product.class));
                break;
            case "product:DELETE":
                fillProductDelete(ent, (Product) old);
                break;
            case "warehouse:CREATE":
                fillWarehouseCreate(ent, unwrap(result));
                break;
            case "warehouse:UPDATE":
                fillWarehouseUpdate(ent, (Warehouse) old, extractEntity(result, Warehouse.class));
                break;
            case "warehouse:DELETE":
                fillWarehouseDelete(ent, (Warehouse) old);
                break;
            case "stockin:CREATE":
                fillStockIn(ent, (StockInOrder) unwrap(result));
                break;
            case "stockin:REVERSE":
                fillStockInReverse(ent, (StockInOrder) unwrap(result), (StockInOrder) old);
                break;
            case "stockout:CREATE":
                fillStockOut(ent, (StockOutOrder) unwrap(result));
                break;
            case "stockout:REVERSE":
                fillStockOutReverse(ent, (StockOutOrder) unwrap(result), (StockOutOrder) old);
                break;
            case "category:CREATE":
                fillCategoryCreate(ent, unwrap(result));
                break;
            case "category:UPDATE":
                fillCategoryUpdate(ent, (Category) old, extractEntity(result, Category.class));
                break;
            case "category:DELETE":
                fillCategoryDelete(ent, (Category) old);
                break;
            case "stocktake:POST":
                fillStockTake(ent, (StockTakeOrder) old);
                break;
            default:
                break;
        }

        ent.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(ent);
    }

    // ---------- 结果解包 ----------
    /** Controller 返回的是 Result 包装，取其中的 data */
    private Object unwrap(Object result) {
        if (result instanceof Result) {
            return ((Result<?>) result).getData();
        }
        return result;
    }

    private <T> T extractEntity(Object result, Class<T> clazz) {
        Object data = unwrap(result);
        return clazz.isInstance(data) ? clazz.cast(data) : null;
    }

    // ---------- 操作人 & 请求信息 ----------
    private void fillOperator(com.example.wms_backend.entity.AuditLog ent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            Long userId = (Long) auth.getPrincipal();
            ent.setOperatorId(userId);
            SysUser u = sysUserMapper.findById(userId);
            if (u != null) {
                ent.setOperatorName(u.getUsername());
            }
        }
    }

    private void fillRequest(com.example.wms_backend.entity.AuditLog ent) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ent.setRequestUri(req.getRequestURI());
                ent.setIp(req.getRemoteAddr());
            }
        } catch (Exception ignore) {
            // 拿不到请求信息就不记
        }
    }

    // ---------- 各模块快照 ----------
    private void fillProductCreate(com.example.wms_backend.entity.AuditLog ent, Object data) {
        if (data instanceof Product) {
            Product p = (Product) data;
            ent.setTargetId(p.getId());
            ent.setDetail("新增商品: " + p.getName() + "(" + p.getCode() + ")");
        }
    }

    private void fillProductUpdate(com.example.wms_backend.entity.AuditLog ent, Product oldP, Product newP) {
        ent.setTargetId(oldP == null ? (newP == null ? null : newP.getId()) : oldP.getId());
        if (oldP != null && newP != null) {
            StringBuilder sb = new StringBuilder("商品修改: ");
            diff(sb, "名称", oldP.getName(), newP.getName());
            diff(sb, "编码", oldP.getCode(), newP.getCode());
            diff(sb, "分类", oldP.getCategoryId(), newP.getCategoryId());
            diff(sb, "单位", oldP.getUnit(), newP.getUnit());
            diff(sb, "规格", oldP.getSpec(), newP.getSpec());
            diff(sb, "售价", oldP.getPrice(), newP.getPrice());
            diff(sb, "预警量", oldP.getWarningQty(), newP.getWarningQty());
            diff(sb, "状态", oldP.getStatus(), newP.getStatus());
            ent.setDetail(truncate(sb.toString()));
        } else {
            ent.setDetail("商品修改 #" + ent.getTargetId());
        }
    }

    private void fillProductDelete(com.example.wms_backend.entity.AuditLog ent, Product oldP) {
        ent.setTargetId(oldP == null ? null : oldP.getId());
        ent.setDetail(oldP != null
                ? "删除商品: " + oldP.getName() + "(" + oldP.getCode() + ")"
                : "删除商品(旧值未捕获)");
    }

    private void fillWarehouseCreate(com.example.wms_backend.entity.AuditLog ent, Object data) {
        if (data instanceof Warehouse) {
            Warehouse w = (Warehouse) data;
            ent.setTargetId(w.getId());
            ent.setTargetNo(w.getCode());
            ent.setDetail("新增仓库: " + w.getName() + "(" + w.getCode() + ")");
        }
    }

    private void fillWarehouseUpdate(com.example.wms_backend.entity.AuditLog ent, Warehouse oldW, Warehouse newW) {
        ent.setTargetId(oldW == null ? (newW == null ? null : newW.getId()) : oldW.getId());
        if (oldW != null && newW != null) {
            StringBuilder sb = new StringBuilder("仓库修改: ");
            diff(sb, "名称", oldW.getName(), newW.getName());
            diff(sb, "编码", oldW.getCode(), newW.getCode());
            diff(sb, "地址", oldW.getAddress(), newW.getAddress());
            diff(sb, "负责人", oldW.getManagerId(), newW.getManagerId());
            diff(sb, "状态", oldW.getStatus(), newW.getStatus());
            ent.setDetail(truncate(sb.toString()));
        } else {
            ent.setDetail("仓库修改 #" + ent.getTargetId());
        }
    }

    private void fillWarehouseDelete(com.example.wms_backend.entity.AuditLog ent, Warehouse oldW) {
        ent.setTargetId(oldW == null ? null : oldW.getId());
        ent.setDetail(oldW != null
                ? "删除仓库: " + oldW.getName() + "(" + oldW.getCode() + ")"
                : "删除仓库(旧值未捕获)");
    }

    private void fillStockIn(com.example.wms_backend.entity.AuditLog ent, StockInOrder o) {
        if (o == null) return;
        ent.setTargetId(o.getId());
        ent.setTargetNo(o.getOrderNo());
        ent.setDetail("入库过账 单号" + o.getOrderNo()
                + " 仓库#" + o.getWarehouseId()
                + " 金额" + o.getTotalAmount());
    }

    private void fillStockOut(com.example.wms_backend.entity.AuditLog ent, StockOutOrder o) {
        if (o == null) return;
        ent.setTargetId(o.getId());
        ent.setTargetNo(o.getOrderNo());
        ent.setDetail("出库过账 单号" + o.getOrderNo()
                + " 仓库#" + o.getWarehouseId()
                + " 金额" + o.getTotalAmount());
    }

    private void fillStockInReverse(com.example.wms_backend.entity.AuditLog ent, StockInOrder rev, StockInOrder old) {
        if (rev == null) return;
        ent.setTargetId(rev.getId());
        ent.setTargetNo(rev.getOrderNo());
        String orig = (old != null && old.getOrderNo() != null) ? old.getOrderNo() : (rev.getReverseOfNo() != null ? rev.getReverseOfNo() : "?");
        ent.setDetail("入库单红冲: 被冲单" + orig + " 原因:" + rev.getRemark());
    }

    private void fillStockOutReverse(com.example.wms_backend.entity.AuditLog ent, StockOutOrder rev, StockOutOrder old) {
        if (rev == null) return;
        ent.setTargetId(rev.getId());
        ent.setTargetNo(rev.getOrderNo());
        String orig = (old != null && old.getOrderNo() != null) ? old.getOrderNo() : (rev.getReverseOfNo() != null ? rev.getReverseOfNo() : "?");
        ent.setDetail("出库单红冲: 被冲单" + orig + " 原因:" + rev.getRemark());
    }

    private void fillCategoryCreate(com.example.wms_backend.entity.AuditLog ent, Object data) {
        if (data instanceof Category) {
            Category c = (Category) data;
            ent.setTargetId(c.getId());
            ent.setDetail("新增分类: " + c.getName()
                    + (c.getParentId() != null && !c.getParentId().equals(0L) ? "(子分类, 父#" + c.getParentId() + ")" : "(顶级分类)"));
        }
    }

    private void fillCategoryUpdate(com.example.wms_backend.entity.AuditLog ent, Category oldC, Category newC) {
        ent.setTargetId(oldC == null ? (newC == null ? null : newC.getId()) : oldC.getId());
        if (oldC != null && newC != null) {
            StringBuilder sb = new StringBuilder("分类修改: ");
            diff(sb, "名称", oldC.getName(), newC.getName());
            diff(sb, "父分类", oldC.getParentId(), newC.getParentId());
            diff(sb, "排序", oldC.getSort(), newC.getSort());
            diff(sb, "状态", oldC.getStatus(), newC.getStatus());
            ent.setDetail(truncate(sb.toString()));
        } else if (oldC != null) {
            // 启停等无返回值场景：记录状态变化
            ent.setDetail("分类修改: " + oldC.getName() + " 状态=>" + oldC.getStatus());
        } else {
            ent.setDetail("分类修改 #" + ent.getTargetId());
        }
    }

    private void fillCategoryDelete(com.example.wms_backend.entity.AuditLog ent, Category oldC) {
        ent.setTargetId(oldC == null ? null : oldC.getId());
        ent.setDetail(oldC != null
                ? "删除分类: " + oldC.getName() + "(父#" + oldC.getParentId() + ")"
                : "删除分类(旧值未捕获)");
    }

    private void fillStockTake(com.example.wms_backend.entity.AuditLog ent, StockTakeOrder o) {
        if (o == null) return;
        ent.setTargetId(o.getId());
        ent.setTargetNo(o.getOrderNo());
        ent.setDetail("盘点过账 单号" + o.getOrderNo() + " 仓库#" + o.getWarehouseId());
    }

    // ---------- 小工具 ----------
    /** 只在旧值≠新值时追加 "字段: 旧->新, " */
    private void diff(StringBuilder sb, String label, Object oldV, Object newV) {
        boolean same = (oldV == null && newV == null)
                || (oldV != null && oldV.equals(newV));
        if (!same) {
            sb.append(label).append(":").append(oldV).append("->").append(newV).append(", ");
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }
}