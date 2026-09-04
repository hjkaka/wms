package com.example.wms_backend.service.impl;

import com.example.wms_backend.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * HelloService 的实现类
 *
 * 这就是"厨师"——菜单(接口)上写了 sayHello，这里写具体怎么做
 */
@Service  // 告诉 Spring："这是一个 Service，帮我创建对象并管理"
         // 加了这个注解，Spring 启动时会自动 new 一个 HelloServiceImpl
         // 然后 Controller 里 @Autowired 就能自动拿到它
public class HelloServiceImpl implements HelloService {

    @Override  // 表示这是重写接口里的方法
    public String sayHello(String name) {
        // 现在的业务逻辑很简单：就是返回一句话
        // 以后这里可能会很复杂：查数据库、算数据、调其他服务等
        return "你好，" + name + "！欢迎来到WMS仓储管理系统！";
    }
}
