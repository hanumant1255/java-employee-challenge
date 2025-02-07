package com.reliaquest.api.config;

import static org.dozer.loader.api.FieldsMappingOptions.customConverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.util.UUIDConverter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@EnableRetry
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(AppProperties appProperties) {
        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
        poolingConnManager.setDefaultMaxPerRoute(
                appProperties.getConnectionManager().getMaxPerRoute());
        poolingConnManager.setMaxTotal(appProperties.getConnectionManager().getMaxRoutes());
        HttpClient httpClient =
                HttpClients.custom().setConnectionManager(poolingConnManager).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Bean
    public DozerBeanMapper dozerBeanMapper() {
        DozerBeanMapper mapper = new DozerBeanMapper();
        List<String> mappingFiles = List.of("dozer/dozer-bean-mappings.xml");
        mapper.setMappingFiles(mappingFiles);
        mapper.addMapping(new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(Employee.class, EmployeeDTO.class).fields("id", "id", customConverter(UUIDConverter.class));
            }
        });
        return mapper;
    }

    @Bean("objectMapper")
    public ObjectMapper initObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.setConfig(
                objectMapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        objectMapper.setConfig(
                objectMapper.getDeserializationConfig().with(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setConfig(
                objectMapper.getDeserializationConfig().with(DeserializationFeature.READ_ENUMS_USING_TO_STRING));
        return objectMapper;
    }
}
