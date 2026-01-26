package com.eric.store.images.controller;

import com.eric.store.images.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final FileStorageService storage;

    @PostMapping(value = "/public", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> addPublic(@RequestPart("file") MultipartFile file) {
        var saved = storage.uploadPublic(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "key", saved.key(),
                "url", saved.publicUrl()
        ));
    }

    @PostMapping(value = "/private", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> addPrivate(@RequestPart("file") MultipartFile file) {
        var saved = storage.uploadPrivate(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "key", saved.key()
        ));
    }

}
