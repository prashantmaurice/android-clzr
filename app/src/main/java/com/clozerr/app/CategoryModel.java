package com.clozerr.app;

/**
 * Created by Aravind S on 5/17/2015.
 */
public class CategoryModel {

    private String mImageId;
    private String mTitle;

    public CategoryModel() {}

    public CategoryModel(String title, String imageId) {
        mImageId = imageId;
        mTitle = title;
    }

    public String getImageId() {
        return mImageId;
    }

    public void setImageId(String mImageId) {
        this.mImageId = mImageId;
    }

    public String getTitle() {
        return mTitle;
    }
}
