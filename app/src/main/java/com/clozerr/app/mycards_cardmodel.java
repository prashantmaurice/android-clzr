package com.clozerr.app;

/**
 * Created by Girish on 1/7/2015.
 */
public class mycards_cardmodel {



    private String visitno;
    private String offer;


    public mycards_cardmodel( String visitno, String offer) {

        this.visitno = visitno;
        this.offer = offer;

    }


    public String getoffer() {
        return offer;
    }
    public void setDesc(String desc) {
        this.offer = desc;
    }
    public String getvisitno() {
        return visitno;
    }
    public void setvisitno(String title) {
        this.visitno = visitno;
    }
    @Override
    public String toString() {
        return visitno + "\n" + offer;
    }

}