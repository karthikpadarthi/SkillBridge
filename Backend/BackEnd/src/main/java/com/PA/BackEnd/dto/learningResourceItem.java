package com.PA.BackEnd.dto;

public class learningResourceItem {
    private String title;
    private String provider;
    private String url;
    private boolean free;
    private int estimatedHours;

    public learningResourceItem() {}

    public learningResourceItem(String title, String provider, String url, boolean free, int estimatedHours) {
        this.title = title;
        this.provider = provider;
        this.url = url;
        this.free = free;
        this.estimatedHours = estimatedHours;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
}
