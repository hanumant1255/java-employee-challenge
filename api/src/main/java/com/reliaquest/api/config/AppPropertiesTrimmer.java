package com.reliaquest.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class AppPropertiesTrimmer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        for (var propertySource : propertySources) {
            if (propertySource instanceof MapPropertySource
                    && propertySource.getName().contains("application")) {
                Map<String, Object> originalProperties = ((MapPropertySource) propertySource).getSource();
                Map<String, Object> trimmedProperties = new HashMap<>();
                for (var entry : originalProperties.entrySet()) {
                    Object trimmedValue = (Objects.isNull(entry.getValue()))
                            ? null
                            : entry.getValue().toString().trim();
                    trimmedProperties.put(entry.getKey(), trimmedValue);
                }
                propertySources.replace(
                        propertySource.getName(), new MapPropertySource(propertySource.getName(), trimmedProperties));
            }
        }
    }
}
