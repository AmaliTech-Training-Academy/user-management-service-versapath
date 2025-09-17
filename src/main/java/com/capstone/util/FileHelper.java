package com.capstone.util;

import com.capstone.dto.response.UserProfileDto;
import com.capstone.exception.FileException;
import com.capstone.model.User;
import com.capstone.service.PreSignedUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileHelper {
    @Value("${FILE_MAX_SIZE:10}")
    int maxFileSize;

    public void validateImage(MultipartFile image) {
        long fileSize = DataSize.ofMegabytes(maxFileSize).toBytes();

        if (image.isEmpty()) {
            throw new FileException("File is empty");
        }

        if (image.getSize() > fileSize) {
            throw new FileException("File size exceeds " + maxFileSize + "MB limit");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileException("Only image files are allowed");
        }
    }

    public void generatePresignedUrl(User user,
                                     UserProfileDto responseDto,
                                     PreSignedUrlService preSignedUrlService) {
        String presignedUrl = generatePresignedUrlIfExists(user, preSignedUrlService);
        if (presignedUrl != null) {
            responseDto.setProfilePictureUrl(presignedUrl);
        }
    }

    public String generatePresignedUrlIfExists(User user, PreSignedUrlService preSignedUrlService) {
        if (user.getProfilePictureUrl() != null) {
            return preSignedUrlService.generatePresignedUrl(user.getProfilePictureUrl());
        }
        return null;
    }
}