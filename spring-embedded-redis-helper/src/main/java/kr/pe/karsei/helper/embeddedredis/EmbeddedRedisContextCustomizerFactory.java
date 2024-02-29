package kr.pe.karsei.helper.embeddedredis;

import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

public class EmbeddedRedisContextCustomizerFactory implements ContextCustomizerFactory {
    private static final ContextCustomizer contextCustomizer = new EmbeddedRedisContextCustomizer();

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
                                                     List<ContextConfigurationAttributes> configAttributes) {
        return contextCustomizer;
    }
}
