package com.example.wms_backend.service;

/**
 * HelloService 接口
 *
 * 为什么是接口而不是直接写实现类？
 * - 接口就像"菜单"，只定义"有什么菜"，不写"怎么做"
 * - 具体怎么做在 HelloServiceImpl 里
 * - 好处：以后如果要换实现（比如中文版换英文版），只改 Impl，接口不用动
 *
 * 这是 Java 开发的规范：面向接口编程
 */
public interface HelloService {

    /**
     * 打招呼方法
     * @param name 名字
     * @return 打招呼的话
     */
    String sayHello(String name);
}
