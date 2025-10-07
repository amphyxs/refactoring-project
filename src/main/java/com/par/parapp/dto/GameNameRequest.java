package com.par.parapp.dto;

public class GameNameRequest {
    private String gameName;

    private Boolean useBonuses;

    public GameNameRequest(String gameName, Boolean useBonuses) {
        this.gameName = gameName;
        this.useBonuses = useBonuses;
    }

    public Boolean getUseBonuses() {
        return useBonuses;
    }

    public void setUseBonuses(Boolean useBonuses) {
        this.useBonuses = useBonuses;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
