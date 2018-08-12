package com.tuyu;

import com.tuyu.zk.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * 监听器，实现ServletContextListener接口，需要在web.xml文件中配置该监听器
 * 如果实现InitializingBean接口，当bean初始化完成也可以连接zookeeper,
 * 但是此方法不能获得ServletContext对象，故不合适
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
    private  String ip;
    @Value("${server.port}")
    private  String port;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        if (applicationContext.getParent() != null) {

            logger.info("not root application context...");
            RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);

            Map<RequestMappingInfo, HandlerMethod> infoMap = mapping.getHandlerMethods();
            logger.info("listen a servlet context start up at " + ip + ":" + port);
            if (ip == null || port == null) {
                logger.error("the ip or port of web server is null");
                return;
            }
            for (RequestMappingInfo info : infoMap.keySet()) {
                String serviceName = info.getName();
                logger.info("-----------------"+serviceName);
                // 如果@RequestMapping注解没有配置name属性，通过info.getName取到的将是null
                if (serviceName != null) {
                    //注册服务
                    registry.register(serviceName, String.format("%s:%s", ip, port));
                    logger.info("registry a service: " + serviceName);
                }
            }
        } else {
            logger.info("root application context.");
        }
    }
}
