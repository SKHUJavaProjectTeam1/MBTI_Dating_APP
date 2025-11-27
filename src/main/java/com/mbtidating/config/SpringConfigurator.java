package com.mbtidating.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @ServerEndpoint 에서 Spring @Autowired 를 가능하게 해주는 설정 클래스
 */
@Component
public class SpringConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        SpringConfigurator.context = ctx;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }
}
