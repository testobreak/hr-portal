package com.acme.hrms.common.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "hrms.storage.enabled", havingValue = "false", matchIfMissing = true)
    public StorageService noopStorageService() {
        return new NoopStorageService();
    }

    @Bean
    @ConditionalOnProperty(name = "hrms.storage.enabled", havingValue = "true")
    public S3Presigner s3Presigner(StorageProperties props) {
        return S3StorageService.presignerFrom(props);
    }

    @Bean
    @ConditionalOnProperty(name = "hrms.storage.enabled", havingValue = "true")
    public StorageService s3StorageService(S3Presigner presigner, StorageProperties props) {
        return new S3StorageService(presigner, props.getPresignTtlSeconds());
    }
}
