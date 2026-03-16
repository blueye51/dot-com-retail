package com.eric.store.ratings.mapper;

import com.eric.store.ratings.dto.RatingDto;
import com.eric.store.ratings.dto.RatingRequest;
import com.eric.store.ratings.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public Rating toRating(RatingRequest req) {
        Rating rating = new Rating();
        rating.setScore(req.score());
        rating.setComment(req.comment());
        return rating;
    }

    public RatingDto toDto(Rating rating) {
        return new RatingDto(
                rating.getId(),
                rating.getUser().getId(),
                rating.getProduct().getId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
