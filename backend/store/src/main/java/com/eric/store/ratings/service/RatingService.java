package com.eric.store.ratings.service;

import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductRepository;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public Rating create(RatingRequest req, UUID userId) {
        Rating rating = ratingMapper.toRating(req);
        Product product = productService.findById(req.productId());
        rating.setProduct(product);
        rating.setUser(userService.findById(userId));
        Rating saved = ratingRepository.save(rating);
        recalculateRatingStats(product);
        return saved;
    }

    @Transactional(readOnly = true)
    public RatingDto getById(UUID id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating", id));
        return ratingMapper.toDto(rating);
    }

    @Transactional(readOnly = true)
    public List<RatingDto> getByUserId(UUID userId) {
        return ratingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ratingMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RatingDto> getByProductId(UUID productId) {
        return ratingRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(ratingMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RatingDto> getReviewsByProductId(UUID productId, UUID currentUserId) {
        return ratingRepository.findReviewsByProductId(productId).stream()
                .map(r -> ratingMapper.toDto(r, currentUserId))
                .toList();
    }

    @Transactional
    public RatingDto voteHelpful(UUID ratingId, UUID userId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        if (rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot vote on your own review");
        }
        if (!rating.getVotedUserIds().add(userId)) {
            rating.getVotedUserIds().remove(userId);
            rating.setHelpfulCount(rating.getHelpfulCount() - 1);
        } else {
            rating.setHelpfulCount(rating.getHelpfulCount() + 1);
        }
        return ratingMapper.toDto(ratingRepository.save(rating), userId);
    }

    @Transactional
    public RatingDto update(UUID id, RatingRequest req, UUID userId) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating", id));
        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own ratings");
        }
        rating.setScore(req.score());
        rating.setComment(req.comment());
        Rating saved = ratingRepository.save(rating);
        recalculateRatingStats(saved.getProduct());
        return ratingMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating", id));
        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own ratings");
        }
        Product product = rating.getProduct();
        ratingRepository.delete(rating);
        ratingRepository.flush();
        recalculateRatingStats(product);
    }

    // ── Admin methods ──

    @Transactional(readOnly = true)
    public Page<RatingDto> getAllReviewsForAdmin(int page, int size) {
        return ratingRepository.findAllReviewsForAdmin(PageRequest.of(page, size))
                .map(ratingMapper::toAdminDto);
    }

    @Transactional
    public RatingDto toggleHidden(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        rating.setHidden(!rating.isHidden());
        Rating saved = ratingRepository.save(rating);
        recalculateRatingStats(saved.getProduct());
        return ratingMapper.toAdminDto(saved);
    }

    @Transactional
    public void adminDelete(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        Product product = rating.getProduct();
        ratingRepository.delete(rating);
        ratingRepository.flush();
        recalculateRatingStats(product);
    }

    private void recalculateRatingStats(Product product) {
        product.setAverageRating(ratingRepository.averageScoreByProductId(product.getId()));
        product.setTotalRatings(ratingRepository.countVisibleByProductId(product.getId()));
        productRepository.save(product);
    }
}
