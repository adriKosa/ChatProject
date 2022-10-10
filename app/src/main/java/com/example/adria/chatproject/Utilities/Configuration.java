package com.example.adria.chatproject.Utilities;

public class Configuration {
    private String label;

    private int icon;

    public Configuration(String label, int icon){
        this.label = label;
        this.icon = icon;
    }

    public String getLabel(){
        return this.label;
    }

    public int getIcon(){
        return this.icon;
    }
}
