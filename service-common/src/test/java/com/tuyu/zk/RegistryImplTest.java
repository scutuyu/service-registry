package com.tuyu.zk;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

/**
 * @author tuyu
 * @date 8/11/18
 * Talk is cheap, show me the code.
 */
public class RegistryImplTest {


    @Test
    public void testContext() {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        Registry registry = (Registry) context.getBean("registry");
        System.out.println(registry);
    }

}