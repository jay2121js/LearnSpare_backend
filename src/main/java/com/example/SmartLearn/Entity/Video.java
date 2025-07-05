package com.example.SmartLearn.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Data

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "videos")
public class Video {

    @PrePersist
    public void init(){
        uploadDate = LocalDateTime.now();
        views = 0;
        likes = 0;
        hslReady = false;
        embedVideo = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Float duration; // in seconds
    private LocalDateTime uploadDate;

    private String videoUrl;  // Direct MP4 URL
    private String hlsUrl;    // HLS streaming URL
    private double fileSize; // in MB
    private String videoPublicId;
    private Boolean hslReady; //
    @ManyToOne// ← LAZY loading here
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    private Boolean embedVideo;
    private String notesPublicId;
    private String notesUrl;
    private int views;
    private int likes;

}
