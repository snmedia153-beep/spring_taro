package com.example.demo.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;
@Service
@RequiredArgsConstructor
public class R2Service {
    private final S3Template s3Template;

    @Value("${r2.bucket-name}")
    private String bucket;

    public String uploadFile(MultipartFile file, String storedFileName) throws IOException {
        // 1. 리사이징 처리 (예: 가로 최대 1200px, 품질 80%)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(800, 800) // 가로 세로 최대 비율 유지
                .outputFormat("jpg") // 용량이 적은 jpg로 변환
                .outputQuality(0.7)  // 품질 80%
                .toOutputStream(outputStream);

        byte[] bytes = outputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        // 2. R2 업로드
        s3Template.upload(bucket, storedFileName, inputStream,
                ObjectMetadata.builder()
                        .contentType("image/jpeg")
                        .contentLength((long) bytes.length)
                        .build());

        return storedFileName;
        /*
        // S3Template을 사용하여 R2 버킷에 업로드
        s3Template.upload(bucket, storedFileName, file.getInputStream(),
                ObjectMetadata.builder().contentType(file.getContentType()).build());

        // 업로드된 파일의 경로(URL) 반환 (필요 시)
        return storedFileName;
         */
    }
    public void deleteFile(String storedFileName) {
        try {
            s3Template.deleteObject(bucket, storedFileName);
        } catch (Exception e) {
            System.out.println("파일 삭제 실패: " + e.getMessage());
        }
    }
}