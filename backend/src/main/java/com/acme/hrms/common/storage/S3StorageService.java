package com.acme.hrms.common.storage;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * S3-compatible presigned URLs (MinIO or AWS).
 */
public class S3StorageService implements StorageService {

    private final S3Presigner presigner;
    private final int maxTtlSeconds;

    public S3StorageService(S3Presigner presigner, int maxTtlSeconds) {
        this.presigner = presigner;
        this.maxTtlSeconds = Math.min(maxTtlSeconds, 300);
    }

    @Override
    public URL presignPut(String bucket, String key, String contentType, Duration ttl) {
        Duration use = cap(ttl);
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        return presigner.presignPutObject(
                        PutObjectPresignRequest.builder()
                                .signatureDuration(use)
                                .putObjectRequest(put)
                                .build())
                .url();
    }

    @Override
    public URL presignGet(String bucket, String key, Duration ttl) {
        Duration use = cap(ttl);
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return presigner.presignGetObject(
                        GetObjectPresignRequest.builder()
                                .signatureDuration(use)
                                .getObjectRequest(get)
                                .build())
                .url();
    }

    private Duration cap(Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            return Duration.ofSeconds(maxTtlSeconds);
        }
        long seconds = Math.min(ttl.getSeconds(), maxTtlSeconds);
        return Duration.ofSeconds(Math.max(1, seconds));
    }

    /**
     * Build presigner from properties (path-style for MinIO when endpoint is set).
     */
    public static S3Presigner presignerFrom(StorageProperties props) {
        var creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));

        var builder = S3Presigner.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(creds);

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()))
                    .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                            .pathStyleAccessEnabled(props.isPathStyleAccess())
                            .build());
        }

        return builder.build();
    }
}
