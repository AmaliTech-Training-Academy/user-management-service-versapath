package com.capstone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PreSignedUrlService {
    private final S3Presigner s3Presigner;

    @Value("${BUCKET_NAME}")
    private String bucketName;

    //generate a pre-signed url each time a user fetch the image.
    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)) // valid for 15 mins
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}