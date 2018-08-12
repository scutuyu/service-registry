package com.tuyu;

import com.tuyu.zk.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**
 * 监听器
 * @author tuyu
 * @date 8/10/18
 * Talk is cheap, show me the code.
 */
@Component
public class WebListener implements ApplicationListener<ContextRefreshedEvent>{

    private static final Logger logger = LoggerFactory.getLogger(WebListener.class);

    @Autowired
    private Registry registry;

    @Value("${server.ip}")
    private String ip;
    @Value("${server.port}")
    private String port;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (ip == null || port == null) {
            logger.error("the ip or port of server is null...");
            return;
        }
        ApplicationContext context = contextRefreshedEvent.getApplicationContext();
        if (context.getParent() != null) {
            logger.info("the web application context...");
            RequestMappingHandlerMapping mappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
            if (mappingHandlerMapping == null) {
                logger.error("can not get bean requestMappingHandlerMapping from applicationContext...");
                return;
            }
            Map<RequestMappingInfo, HandlerMethod> methods = mappingHandlerMapping.getHandlerMethods();
            if (methods.size() == 0) {
                logger.info("no controller method to be registry");
                return;
            }
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : methods.entrySet()) {
                RequestMappingInfo key = entry.getKey();
                HandlerMethod value = entry.getValue();
                String serviceName = key.getName();
                if (serviceName == null) {
                    logger.error("please set attribute name or @RequestMapping");
                    continue;
                }
                String serviceAddress = ip + ":" + port;
                registry.register(serviceName, serviceAddress);
                logger.info("registry a serviceName: {}, with serviceAddress: {}", serviceName, serviceAddress);
            }
        } else {
            logger.info("the root application context");
        }
    }
}
