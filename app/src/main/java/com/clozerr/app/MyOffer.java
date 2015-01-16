package com.clozerr.app;

public class MyOffer {
    private int stamps;
    private String caption;

    public int getStamps() {
        return stamps;
    }

    public void setStamps(int stamps) {
        this.stamps = stamps;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public MyOffer(String caption,int stamps) {
        this.caption = caption;
        this.stamps = stamps;
    }
}