package com.clozerr.app.Models;

import com.clozerr.app.Utils.Constants;

public class NavObject {
    public int iconResId;
    public Constants.NavListId listId;
    public String title;
    public int number = 0;
    public NavObject(String title, int iconResId, Constants.NavListId listId){
        this.title = title;
        this.iconResId = iconResId;
        this.listId = listId;
    }
}
