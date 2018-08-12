package com.tuyu.zk;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;


/**
 * 向zookeeper中注册服务
 * @author tuyu
 * @date 8/10/18
 * Talk is cheap, show me the code.
 */
@Component
public class RegistryImpl implements Registry, Watcher, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(RegistryImpl.class);

    private static CountDownLatch latch = new CountDownLatch(1);
    private static final String REGISTRY_PATH = "/registry";
    private static final int SESSION_TIMEOUT = 5000;

    /** 保证zookeeper是单例的，并且断开自动重连,这里没有实现 */
    private ZooKeeper zk;

    /** zookeeper的服务地址 */
    @Value("${zk.connection}")
    private String servers;

    public void setServers(String servers) {
        this.servers = servers;
    }


    public RegistryImpl() {
    }

    public RegistryImpl(String zkServers) {
        logger.info("call constructor with param....");
        boolean b = doConnect(zkServers);
    }

    private boolean doConnect(String zkServers) {
        if (zkServers == null || "".equals(zkServers)) {
            logger.error("zkServers is empty...");
            return false;
        }
        if (zk != null && zk.getState() != ZooKeeper.States.CLOSED) {
            logger.info("can not create duplicate zookeeper instance");
            return false;
        }
        try {
            logger.info("constructor: I get a zk connection: " + servers);
            zk = new ZooKeeper(servers, SESSION_TIMEOUT, this);
            // 由于zk实例化过程是异步的，如果不等到服务器返回连接建立成功就使用zk对象，会发生错误，故使用门栓同步
            latch.await();
            logger.debug("connected to zookeeper");
        } catch (Exception ex) {
            logger.error("create zookeeper client failure", ex);
            return false;
        }
        return true;
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String registryPath = REGISTRY_PATH;
            if (zk.exists(registryPath, false) == null) {
                zk.create(registryPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.debug("create registry node:{}", registryPath);
            }
            //创建服务节点（持久节点）
            String servicePath = registryPath + "/" + serviceName;
            if (zk.exists(servicePath, false) == null) {
                zk.create(servicePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.debug("create service node:{}", servicePath);
            }
            String addressPath = servicePath + "/address-";
            //创建地址临时有序节点（CreateMode.EPHEMERAL_SEQUENTIAL）
            String addressNode = zk.create(addressPath, serviceAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.debug("create address node:{} => {}", addressNode, serviceAddress);
            System.out.printf("create address node:" + addressNode + " => " + serviceAddress);
        } catch (Exception e) {
            logger.error("create node failure", e);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        // 连接成功后，打开门栓，完成zk的实例化
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected)
            latch.countDown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("this {}, registry bean initialized...", this);
        doConnect(this.servers);
    }
}
