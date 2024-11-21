package dev.sitconsulting.library.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class MetricsEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final String PROPERTY_SOURCE_NAME = "defaultMetricsProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("management.metrics.export.prometheus.enabled", "true");
        defaultProperties.put("management.security.enabled", "false");
        defaultProperties.put("management.server.ssl.enabled", "false");
        defaultProperties.put("management.endpoints.web.exposure.include",
                "beans,caches,conditions,configprops,env,health,info,liquibase,mappings,prometheus");

        MapPropertySource target = new MapPropertySource(PROPERTY_SOURCE_NAME, defaultProperties);
        environment.getPropertySources().addLast(target);
    }
}