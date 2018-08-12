# 基于zookeeper的微服务注册

zk-service-registry是城云智慧云平台中间层与后端基础api层架构优化的一个实践，当时为了方便，
直接将后端api硬编码到中间层，这对后面服务的部署，扩展等带来不便，于是想把后端微服务接口注册到
zookeeper中，node去zookeeper中发现服务，实现中间node层与后端解耦。

## 构建步骤
```
# 1. git clone https://github.com/scutuyu/service-registry.git

# 2. 使用IDE导入即可

# 3. cd service-first && mvn jetty:run

# 4. cd service-second && mvn jetty:run
```

## 技术栈
- spring web(4.3.5.RELEASE)
- spring webmvc(4.3.5.RELEASE)
- zookeeper(java client 3.4.6)

## 代码结构
```
├── README.md
├── pom.xml
├── service-common
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── tuyu
│       │   │           └── zk
│       │   │               ├── Registry.java
│       │   │               └── RegistryImpl.java
│       │   └── resources
│       │       ├── applicationContext.xml
│       │       └── log4j.properties
│       └── test
│           └── java
│               └── com
│                   └── tuyu
│                       └── zk
│                           └── RegistryImplTest.java
├── service-first
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── tuyu
│           │           ├── WebListener.java
│           │           └── controller
│           │               └── HelloController.java
│           ├── test
│           │   └── java
│           │       └── com
│           │           └── tuyu
│           │               └── controller
│           │                   └── ControllerTest.java
│           └── webapp
│               └── WEB-INF
│                   ├── applicationContext.xml
│                   ├── config.properties
│                   ├── log4j.properties
│                   ├── springMvc.xml
│                   └── web.xml
└── service-second
    ├── pom.xml
    └── src
        └── main
            ├── java
            │   └── com
            │       └── tuyu
            │           ├── WebListener.java
            │           └── controller
            │               └── HelloController.java
            └── webapp
                └── WEB-INF
                    ├── applicationContext.xml
                    ├── config.properties
                    ├── log4j.properties
                    └── web.xml
```

## 服务注册的思路

实现ApplicationListener接口，监听ContextRefreshEvent事件，当spring上下文初始化完成，
就将所有controller类的接口方法都注册到zookeeper中
核心方法如下：
```java
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
```

## node层调用示例
[示例代码]()

## 踩坑记录
1. 一开始让`WebListener`类实现ServletContextListener接口，在`contextInitialized`方法里注册服务，这样老是报错，
比如`ip`和`port`的值为null，总的来说就是没有搞清楚ContextLoaderListener初始化的上下文和DispatcherServlet
初始化的上下文关系，ContextLoaderListener初始化的上下文加载的bean对于整个应用程序是共享的，它一般加载除controller以外
的所有bean，而DispatcherServlet初始化的上下文加载的bean只是对Spring Web MVC有效的bean，如Controller、HandlerMapping、
HandlerAdapter,只加载Web相关的组件, 作为root application context的子容器；需要让ContextLoadListener初始化根上下文，
之后再让DispatcherServlet初始化对应的web上下文
[参考链接](http://jinnianshilongnian.iteye.com/blog/1602617)
[参考链接](https://blog.csdn.net/en_joker/article/details/78580166)
2. `java.lang.IllegalArgumentException: Circular placeholder reference 'zk.connection' in property definitions`，
这个错误是因为配置文件拷贝和过滤出了问题，我一直用build->resources->resource的方式来进行资源的拷贝，原来该方法适合一般的
maven项目，而对于web项目，需要使用插件maven-war-plugin，通过webResources配置资源的拷贝和过滤
...

## 优势
1. 弥补的后端服务注册的问题，能够灵活的注册服务
2. 使node层与后端解耦，更好的维护代码
3. 更好的提供分布式微服务，同一个服务有多个提供者
 
## 不足
1. 该项目是我结合平时学习，以及网上的[资料](https://github.com/bill1012/microservice/tree/master/zookeeper-demo)实现的，有待实践的检验
2. ...