package com.clozerr.app;

/**
 * Created by srivatsan on 11/7/15.
 */
public class RewardItem {
    String Name;
    String Caption;
    String Description;
    String Image;
    String RewardId;
    public RewardItem(String Name,String Caption,String Description,String Image, String RewardId){
        this.Name = splitCamelCase(Name);
        this.Caption = Caption;
        this.Description = Description;
        this.Image = Image;
        this.RewardId = RewardId;
    }
    static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }
}
