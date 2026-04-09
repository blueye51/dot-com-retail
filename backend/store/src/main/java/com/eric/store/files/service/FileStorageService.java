package com.eric.store.files.service;

import com.eric.store.common.exceptions.IllegalJsonException;
import com.eric.store.common.exceptions.StorageException;
import com.eric.store.files.config.S3Props;
import com.eric.store.files.config.StorageProps;
import com.eric.store.files.entity.FileEntity;
import com.eric.store.files.repository.FileRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import software.amazon.awssdk.core.exception.SdkException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final S3Props props;
    private final StorageProps props2;
    private final FileRepository fileRepository;

    private static final int THUMB_SIZE = 200;
    private static final Set<String> THUMBNAIL_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public record StoredImage(
            String key,
            String publicUrl
    ) { }

    private void validate(MultipartFile file) {
        if (!props2.allowedTypes().contains(file.getContentType())) {
            throw new IllegalJsonException("File type not allowed: " + file.getContentType());
        }
        if (file.getSize() > props2.maxSizeBytes()) {
            throw new IllegalJsonException("File too large");
        }

    }

    @Transactional
    public StoredImage uploadPublic(MultipartFile file) {
        validate(file);
        try {
            String key = "public/" + UUID.randomUUID() + ext(file.getOriginalFilename());
            String url = props.publicBaseUrl() + "/" + key;

            String thumbnailUrl = null;
            if (THUMBNAIL_TYPES.contains(file.getContentType())) {
                thumbnailUrl = generateAndUploadThumbnail(file, key);
            }

            FileEntity entity = new FileEntity();
            entity.setKey(key);
            entity.setUrl(url);
            entity.setThumbnailUrl(thumbnailUrl);
            entity.setContentType(Objects.requireNonNull(file.getContentType()));
            entity.setSizeBytes(file.getSize());
            fileRepository.save(entity);

            put(file, key);
            return new StoredImage(key, url);
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        } catch (SdkException e) {
            throw new StorageException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public StoredImage uploadPrivate(MultipartFile file) {
        validate(file);
        try {
            String key = "private/" + UUID.randomUUID() + ext(file.getOriginalFilename());
            create(key, null, file);
            put(file, key);
            return new StoredImage(key, null);
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        } catch (SdkException e) {
            throw new StorageException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    public FileEntity create(String key, String url,  MultipartFile file) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setKey(key);
        fileEntity.setUrl(url);
        fileEntity.setContentType(Objects.requireNonNull(file.getContentType()));
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

    private String generateAndUploadThumbnail(MultipartFile file, String originalKey) throws IOException {
        return generateAndUploadThumbnailFromBytes(file.getInputStream().readAllBytes(), originalKey);
    }

    public String generateAndUploadThumbnailFromBytes(byte[] imageBytes, String originalKey) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) return null;

        BufferedImage thumb = new BufferedImage(THUMB_SIZE, THUMB_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, THUMB_SIZE, THUMB_SIZE, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumb, "jpg", baos);
        byte[] thumbBytes = baos.toByteArray();

        String thumbKey = thumbKey(originalKey);
        String thumbUrl = props.publicBaseUrl() + "/" + thumbKey;

        var putReq = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(thumbKey)
                .contentType("image/jpeg")
                .cacheControl("public, max-age=31536000")
                .build();

        s3.putObject(putReq, RequestBody.fromInputStream(
                new ByteArrayInputStream(thumbBytes), thumbBytes.length));

        return thumbUrl;
    }

    static String thumbKey(String originalKey) {
        int dot = originalKey.lastIndexOf('.');
        if (dot >= 0) {
            return originalKey.substring(0, dot) + "-thumb.jpg";
        }
        return originalKey + "-thumb.jpg";
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
