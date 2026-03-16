package com.eric.store.ratings.service;

import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.service.ProductService;
import com.eric.store.ratings.dto.RatingDto;
import com.eric.store.ratings.dto.RatingRequest;
import com.eric.store.ratings.entity.Rating;
import com.eric.store.ratings.mapper.RatingMapper;
import com.eric.store.ratings.repository.RatingRepository;
import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final ProductService productService;
    private final UserService userService;

    public Rating create(RatingRequest req, UUID userId) {
        Rating rating = ratingMapper.toRating(req);
        rating.setProduct(productService.findById(req.productId()));
        rating.setUser(userService.findById(userId));
        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public RatingDto getById(UUID id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating", id));
        return ratingMapper.toDto(rating);
    }

    @Transactional(readOnly = true)
    public List<RatingDto> getByUserId(UUID userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(ratingMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RatingDto> getByProductId(UUID productId) {
        return ratingRepository.findByProductId(productId).stream()
                .map(ratingMapper::toDto)
                .toList();
    }

    public RatingDto update(UUID id, RatingRequest req, UUID userId) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating", id));
        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own ratings");
        }
        rating.setScore(req.score());
        rating.setComment(req.comment());
        return ratingMapper.toDto(ratingRepository.save(rating));
    }

    public void delete(UUID id, UUID userId) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating", id));
        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own ratings");
        }
        ratingRepository.delete(rating);
    }
}
