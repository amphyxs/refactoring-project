package com.par.parapp.dto;

public class GamePictures {
    private String pictureShop;

    private String pictureCover;

    private String pictureGameplay1;

    private String pictureGameplay2;

    private String pictureGameplay3;

    public GamePictures(String pictureShop, String pictureCover, String pictureGameplay1, String pictureGameplay2,
            String pictureGameplay3) {
        this.pictureShop = pictureShop;
        this.pictureCover = pictureCover;
        this.pictureGameplay1 = pictureGameplay1;
        this.pictureGameplay2 = pictureGameplay2;
        this.pictureGameplay3 = pictureGameplay3;
    }

    public String getPictureShop() {
        return pictureShop;
    }

    public void setPictureShop(String pictureShop) {
        this.pictureShop = pictureShop;
    }

    public String getPictureCover() {
        return pictureCover;
    }

    public void setPictureCover(String pictureCover) {
        this.pictureCover = pictureCover;
    }

    public String getPictureGameplay1() {
        return pictureGameplay1;
    }

    public void setPictureGameplay1(String pictureGameplay1) {
        this.pictureGameplay1 = pictureGameplay1;
    }

    public String getPictureGameplay2() {
        return pictureGameplay2;
    }

    public void setPictureGameplay2(String pictureGameplay2) {
        this.pictureGameplay2 = pictureGameplay2;
    }

    public String getPictureGameplay3() {
        return pictureGameplay3;
    }

    public void setPictureGameplay3(String pictureGameplay3) {
        this.pictureGameplay3 = pictureGameplay3;
    }
}
