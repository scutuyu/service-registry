package com.tuyu.controller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author tuyu
 * @date 8/10/18
 * Talk is cheap, show me the code.
 */
public class ControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ControllerTest.class);

    @Test
    public void testStart() {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("src/main/webapp/WEB-INF/applicationContext.xml");
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/Users/tuyu/Desktop/test/zk-service-register/service-first/target/classes/applicationContext.xml");
        ApplicationContext context = new StaticWebApplicationContext();
        HelloController bean = context.getBean(HelloController.class);
        Object hello = bean.hello();
        logger.info("-->" + hello);
    }

    @Test
    public void testWrite() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new File("src/main/webapp/WEB-INF/test.txt"));
        printWriter.println("hello world");
        printWriter.flush();
        printWriter.close();
    }

    @Test
    public void testRead() throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(new File("src/main/webapp/WEB-INF/applicationContext.xml")));
        while (scanner.hasNextLine()) {
            logger.info(scanner.nextLine());
        }
    }
}
