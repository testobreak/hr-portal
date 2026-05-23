package com.acme.hrms.common.storage;

import java.net.URL;
import java.time.Duration;

/**
 * Object storage abstraction. Implementations talk to S3-compatible APIs.
 */
public interface StorageService {

    /**
     * Issue a time-limited URL for the client to upload bytes via HTTP PUT.
     */
    URL presignPut(String bucket, String key, String contentType, Duration ttl);

    /**
     * Issue a time-limited URL for the client to download via HTTP GET.
     */
    URL presignGet(String bucket, String key, Duration ttl);
}
