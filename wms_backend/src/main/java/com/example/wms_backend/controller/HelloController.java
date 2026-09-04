package com.example.wms_backend.controller;

import com.example.wms_backend.common.Result;
import com.example.wms_backend.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloController 控制器
 *
 * 这是"接待员"——接收前端请求，调用 Service，返回结果
 * 前端发请求 → Controller 接收 → 调用 Service 处理 → 返回结果给前端
 */
@RestController  // 告诉 Spring：这是一个 Controller
                 // @RestController = @Controller + @ResponseBody
                 // @ResponseBody 表示方法返回值自动转成 JSON 发给前端
public class HelloController {

    // @Autowired：Spring 自动把 HelloServiceImpl 注入进来
    // 你不需要自己 new HelloServiceImpl()，Spring 帮你创建好了
    // 这叫"依赖注入"（DI），是 Spring 最核心的功能
    @Autowired
    private HelloService helloService;

    /**
     * GET 请求接口：/api/hello
     *
     * 前端访问方式：
     *   浏览器地址栏输入：http://localhost:8888/api/hello?name=小明
     *
     * 返回结果（JSON格式）：
     *   {"code":200,"message":"success","data":"你好，小明！欢迎来到WMS仓储管理系统！"}
     *
     * @GetMapping 专门处理 GET 请求（查数据用 GET）
     * @RequestParam 从 URL 参数里取值，比如 ?name=小明
     *   defaultValue = "同学" 表示如果不传 name 参数，默认值是"同学"
     */
    @GetMapping("/api/hello")
    public Result<String> hello(@RequestParam(defaultValue = "同学") String name) {
        // 第1步：调用 Service 处理业务逻辑
        String message = helloService.sayHello(name);
        // 第2步：把结果包装成统一格式返回
        return Result.success(message);
    }
}
