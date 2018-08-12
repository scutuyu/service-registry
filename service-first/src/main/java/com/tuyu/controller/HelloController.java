package com.tuyu.controller;

import com.tuyu.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *     刚开始运行web服务时，根本访问不到路径，原因是mvc的注解驱动没有添加，
 *     注意要添加正确的命名空间，是mvc的注解驱动
 * </p>
 * @author tuyu
 * @date 8/10/18
 * Talk is cheap, show me the code.
 */
@RequestMapping(path = "/")
@RestController
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private WebListener webListener;

    /**
     * RequestMapping注解需要设置name属性，之后会把该属性值注册到zookeeper中
     * 即zookeeper中一个临时节点有序节点
     * @return
     */
    @RequestMapping(name = "HelloService", path = "/hello")
    public Object hello() {
        return "hello tuyu";
    }

}
