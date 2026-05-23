package com.acme.hrms.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3-compatible storage (MinIO locally, AWS in production).
 *
 * <p>When {@code enabled} is false, {@link NoopStorageService} is used so CI and
 * integration tests do not require a live object store.
 */
@ConfigurationProperties(prefix = "hrms.storage")
public class StorageProperties {

    /**
     * When false, presigned URLs are stubs and no network calls are made.
     */
    private boolean enabled = false;

    private String bucket = "hrms-documents";

    private String region = "us-east-1";

    /**
     * Optional override for MinIO / LocalStack, e.g. {@code http://localhost:9000}.
     */
    private String endpoint = "";

    private String accessKey = "";

    private String secretKey = "";

    private boolean pathStyleAccess = true;

    /** Max validity for presigned PUT/GET; capped at 5 minutes per architecture. */
    private int presignTtlSeconds = 300;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public int getPresignTtlSeconds() {
        return presignTtlSeconds;
    }

    public void setPresignTtlSeconds(int presignTtlSeconds) {
        this.presignTtlSeconds = presignTtlSeconds;
    }
}
