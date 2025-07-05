package com.example.SmartLearn.DTO;

import lombok.Data;

@Data
public class VideoDTO {
    private String id;

    public VideoDTO(String id, String title, String description, Float duration, Boolean hslReady, String hlsUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.hslReady = hslReady;
        this.hlsUrl = hlsUrl;
    }

    private String title;
    private String description;
    private Float duration;
    private Boolean hslReady;
    private String hlsUrl;

    public VideoDTO(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
