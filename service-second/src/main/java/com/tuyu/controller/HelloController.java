package com.tuyu.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author tuyu
 * @date 8/10/18
 * Talk is cheap, show me the code.
 */
@RequestMapping(path = "/")
@RestController
public class HelloController {


    @RequestMapping(path = "/hello")
    public Object hello() {

        return "hello tuyu";
    }

}
