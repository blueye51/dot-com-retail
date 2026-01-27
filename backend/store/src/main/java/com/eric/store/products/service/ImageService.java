package com.eric.store.products.service;

import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.files.entity.FileEntity;
import com.eric.store.files.repository.FileRepository;
import com.eric.store.products.dto.ImageCreate;
import com.eric.store.products.entity.Product;
import com.eric.store.products.entity.Image;
import com.eric.store.products.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {
    private final ImageRepository imageRepository;
    private final FileRepository fileRepository;

    private List<Image> getImagesByProductIdAsc(UUID productId) {
        return imageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
    }

    public List<ImageCreate> getImageDtosByProductIdAsc(UUID productId) {
        List<Image> images = imageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
        return images.stream().map(ImageCreate::from).toList();
    }

    public Image create(ImageCreate imageCreate, Product product) {
        FileEntity file = fileRepository.findById(imageCreate.fileKey())
                .orElseThrow(() -> new NotFoundException("FileEntity", imageCreate.fileKey()));
        Image image = new Image(
                file,
                imageCreate.sortOrder()
        );
        return imageRepository.save(image);
    }
}
