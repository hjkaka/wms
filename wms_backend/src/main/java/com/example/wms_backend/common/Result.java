package com.example.wms_backend.common;

import lombok.Data;

/**
 * 统一返回格式
 * 所有接口返回的数据都包装成这个格式：{code, message, data}
 * 就像餐厅所有菜都用一样的盘子装，前端处理起来才方便
 *
 * @param <T> 泛型：data 可以是任意类型（String、List、对象等）
 */
@Data  // Lombok 注解：自动生成 getter、setter、toString 等方法
public class Result<T> {

    // 状态码：200=成功，其他数字=失败（比如 400=参数错误，500=服务器错误）
    private Integer code;

    // 提示信息：成功是 "success"，失败是具体错误原因
    private String message;

    // 真正的数据：比如查询到的用户信息、商品列表等
    private T data;

    /**
     * 成功返回（带数据）
     * 用法：return Result.success(user);
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（不带数据）
     * 用法：return Result.success();  比如删除操作成功后不需要返回数据
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败返回
     * 用法：return Result.error(400, "用户名不存在");
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
