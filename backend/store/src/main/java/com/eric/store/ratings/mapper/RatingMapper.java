package com.eric.store.ratings.mapper;

import com.eric.store.ratings.dto.RatingDto;
import com.eric.store.ratings.dto.RatingRequest;
import com.eric.store.ratings.entity.Rating;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RatingMapper {

    public Rating toRating(RatingRequest req) {
        Rating rating = new Rating();
        rating.setScore(req.score());
        rating.setComment(req.comment());
        return rating;
    }

    public RatingDto toDto(Rating rating) {
        return toDto(rating, null);
    }

    public RatingDto toDto(Rating rating, UUID currentUserId) {
        boolean voted = currentUserId != null && rating.getVotedUserIds().contains(currentUserId);
        return new RatingDto(
                rating.getId(),
                rating.getUser().getId(),
                rating.getUser().getName(),
                rating.getProduct().getId(),
                null,
                rating.getScore(),
                rating.getComment(),
                rating.getHelpfulCount(),
                voted,
                rating.isHidden(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }

    public RatingDto toAdminDto(Rating rating) {
        return new RatingDto(
                rating.getId(),
                rating.getUser().getId(),
                rating.getUser().getName(),
                rating.getProduct().getId(),
                rating.getProduct().getName(),
                rating.getScore(),
                rating.getComment(),
                rating.getHelpfulCount(),
                false,
                rating.isHidden(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
