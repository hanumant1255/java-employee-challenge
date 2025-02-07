package com.reliaquest.api.config;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties implements Validator, ApplicationContextAware {
    public static final String APP_S_IS_REQUIRED_IT_CANNOT_BE_MISSING_OR_EMPTY =
            "app.%s is required. It cannot be missing or empty";

    private ApplicationContext applicationContext;

    private ConnectionManager ConnectionManager = new ConnectionManager();

    ConnectionManager connectionManager = new ConnectionManager();

    MockEmployeeService mockEmployeeService = new MockEmployeeService();

    @Data
    @NoArgsConstructor
    public static class ConnectionManager {
        private int maxRoutes;
        private int maxPerRoute;
    }

    @Data
    public static class MockEmployeeService {
        private String url;
        private int retryAttempts;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AppProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        String notBlankErrorCode = "required-non-blank";
        for (String property : Set.of("mock-employee-service.url")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(
                    errors,
                    property,
                    notBlankErrorCode,
                    String.format(APP_S_IS_REQUIRED_IT_CANNOT_BE_MISSING_OR_EMPTY, property));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void printApplicationProperties() {
        ConfigurableEnvironment env = (ConfigurableEnvironment) applicationContext.getEnvironment();
        log.info(
                "===================================================================================================================================");
        log.info(
                "====== Loading all Configuration Properties from application.properties and application-config.properties =========================");
        Set<String> secrets = new HashSet<>(List.of("add.secrets.properties"));

        env.getPropertySources().stream()
                .filter(ps -> ps instanceof MapPropertySource && (ps.getName().contains("application")))
                .map(ps -> ((MapPropertySource) ps).getSource().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .forEach(key -> log.info("{}={}", key, !secrets.contains(key) ? env.getProperty(key) : "*****"));
        log.info(
                "=================================================================================================================================");
    }
}
