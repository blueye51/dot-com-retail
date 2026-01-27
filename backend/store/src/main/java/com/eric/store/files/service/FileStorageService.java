package com.eric.store.files.service;

import com.eric.store.common.exceptions.StorageException;
import com.eric.store.files.config.S3Props;
import com.eric.store.files.entity.FileEntity;
import com.eric.store.files.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FileStorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final S3Props props;
    private final FileRepository fileRepository;

    public record StoredImage(String key, String publicUrl) {
    }

    public StoredImage uploadPublic(MultipartFile file) {
        try {
            String key = "public/" + UUID.randomUUID() + ext(file.getOriginalFilename());
            String url = props.publicBaseUrl() + "/" + key;
            create(key, url,  file);
            put(file, key);
            return new StoredImage(key, url);
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        }
    }

    public StoredImage uploadPrivate(MultipartFile file) {
        try {
            String key = "private/" + UUID.randomUUID() + ext(file.getOriginalFilename());
            create(key, null, file);
            put(file, key);
            return new StoredImage(key, null);
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        }
    }

    public FileEntity create(String key, String url,  MultipartFile file) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setKey(key);
        fileEntity.setUrl(url);
        fileEntity.setContentType(file.getContentType());
        fileEntity.setSizeBytes(file.getSize());
        return fileRepository.save(fileEntity);
    }

    public URL presignedGetUrl(String key, Duration ttl) {
        var getReq = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();

        var presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getReq)
                .build();

        return presigner.presignGetObject(presignReq).url();
    }

    private void put(MultipartFile file, String key) throws IOException {
        var putReq = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType(file.getContentType())
                .cacheControl("public, max-age=31536000")
                .build();

        s3.putObject(putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private static String ext(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return (i >= 0) ? filename.substring(i) : "";
    }

}
