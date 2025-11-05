package com.par.parapp.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.par.parapp.dto.ReviewsResponse;
import com.par.parapp.exception.ResourceNotFoundException;
import com.par.parapp.model.Game;
import com.par.parapp.model.Review;
import com.par.parapp.model.User;
import com.par.parapp.repository.ReviewRepository;

@Service
public class ReviewService {
    private final ShopService shopService;

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository, ShopService shopService) {
        this.reviewRepository = reviewRepository;
        this.shopService = shopService;
    }

    public void saveReview(User user, Game game, String reviewText) {
        reviewRepository.save(new Review(user, game, reviewText, new Timestamp(System.currentTimeMillis())));
    }

    public List<Review> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAll(pageable).getContent();
    }

    public List<Review> getAllReviewsByGameName(String gameName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByGameName(gameName, pageable)
                .orElseThrow(ResourceNotFoundException::new);
        return reviewPage.getContent();
    }

    public List<ReviewsResponse> getReviewsByCondition(String selectedGame, int page, int size) {
        List<Review> reviewList;
        if (selectedGame.isEmpty())
            reviewList = getAllReviews(page, size);
        else
            reviewList = getAllReviewsByGameName(selectedGame, page, size);

        List<ReviewsResponse> reviewsResponses = new ArrayList<>();
        reviewList.forEach(review -> reviewsResponses.add(new ReviewsResponse(review.getUser().getLogin(),
                review.getSendDate().toString().substring(0,
                        review.getSendDate().toString().indexOf('.')),
                review.getReviewText(),
                shopService.getGamePicture(review.getGame().getName()), review.getGame().getName())));
        reviewsResponses.sort(Comparator.comparing(ReviewsResponse::getSendDate));
        Collections.reverse(reviewsResponses);
        return reviewsResponses;
    }
}
