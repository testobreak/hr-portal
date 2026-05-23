package com.acme.hrms.common.storage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub storage for tests and dev without MinIO. Returns deterministic fake URLs.
 */
public class NoopStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(NoopStorageService.class);

    @Override
    public URL presignPut(String bucket, String key, String contentType, Duration ttl) {
        log.debug("noop presignPut bucket={} key={} contentType={}", bucket, key, contentType);
        return fakeUrl("put", bucket, key);
    }

    @Override
    public URL presignGet(String bucket, String key, Duration ttl) {
        log.debug("noop presignGet bucket={} key={}", bucket, key);
        return fakeUrl("get", bucket, key);
    }

    private static URL fakeUrl(String op, String bucket, String key) {
        try {
            return URI.create("https://storage.invalid/" + op + "/" + bucket + "/" + key).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
