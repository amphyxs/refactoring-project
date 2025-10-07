package com.par.parapp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ReviewRequest {

    @NotBlank(message = "Выберите игру для отзыва!")
    @NotNull(message = "Выберите игру для отзыва!")
    private String gameName;

    @NotBlank(message = "Отзыв не может быть пустым!")
    @Size(min = 1, max = 1000, message = "Укажите отзыв! От 1 до 1000 символов")
    private String reviewText;

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
