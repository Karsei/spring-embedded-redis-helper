package kr.pe.karsei.helper.embeddedredis;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public class EmbeddedRedisContextCustomizer implements ContextCustomizer {
    private static EmbeddedRedis embeddedRedis;
    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public void customizeContext(ConfigurableApplicationContext context,
                                 MergedContextConfiguration mergedConfig) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        Assert.isInstanceOf(DefaultSingletonBeanRegistry.class, beanFactory);

        ConfigurableEnvironment environment = context.getEnvironment();

        boolean isJakarta = false;

        // 패스워드 확인
        String password = null;
        if (StringUtils.hasText(environment.getProperty("spring.data.redis.password"))) {
            password = environment.getProperty("spring.data.redis.password");
            isJakarta = true;
        } else if (StringUtils.hasText(environment.getProperty("spring.redis.password"))) {
            password = environment.getProperty("spring.redis.password");
        }
        if (!StringUtils.hasText(password)) throw new IllegalArgumentException("Redis Password 가 존재하지 않습니다.");

        // Redis 실행
        lock.lock();
        try {
            if (null == embeddedRedis) {
                embeddedRedis = new EmbeddedRedis(password);
            }
        } catch (IOException e) {
            throw new RuntimeException("Embedded Redis 를 실행하는 과정에서 오류가 발생했습니다.", e);
        } finally {
            lock.unlock();
        }

        // Property 대체
        Properties properties = new Properties();
        properties.put(isJakarta ? "spring.data.redis.port" : "spring.redis.port", embeddedRedis.getPort());
        environment.getPropertySources().addFirst(new PropertiesPropertySource("embeddedRedisProps", properties));

        // 초기화
        String beanName = EmbeddedRedis.class.getName();
        ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(beanName,
                new RootBeanDefinition(EmbeddedRedis.class, () -> embeddedRedis));
    }
}
