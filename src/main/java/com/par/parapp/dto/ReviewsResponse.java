package com.par.parapp.dto;

public class ReviewsResponse {
    private String userLogin;
    private String sendDate;
    private String reviewText;
    private String gamePictureUrl;
    private String gameName;

    public ReviewsResponse(String userLogin, String sendDate, String reviewText, String gamePictureUrl,
            String gameName) {
        this.userLogin = userLogin;
        this.sendDate = sendDate;
        this.reviewText = reviewText;
        this.gamePictureUrl = gamePictureUrl;
        this.gameName = gameName;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getGamePictureUrl() {
        return gamePictureUrl;
    }

    public void setGamePictureUrl(String gamePictureUrl) {
        this.gamePictureUrl = gamePictureUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
