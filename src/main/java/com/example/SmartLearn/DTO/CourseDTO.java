package com.example.SmartLearn.DTO;

import com.example.SmartLearn.Entity.Video;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseDTO {
    private Long courseId;
    private String title;
    private String thumbnailUrl;
    private String difficultyLevel;
    private double price;

    private int lessonCount;
    private Float duration;

    private List<VideoDTO> videos;  // ✅ List of DTOs instead of entity
    private int commentCount;
    private int enrolledUsers;
    private String teacherName;


    // Constructor for summary fetch (now includes lessonCount and duration)
    public CourseDTO(Long courseId, String title, String thumbnailUrl, String difficultyLevel, double price, int lessonCount, Float duration) {
        this.courseId = courseId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.difficultyLevel = difficultyLevel;
        this.price = price;
        this.lessonCount = lessonCount;
        this.duration = duration;
    }
    public CourseDTO(Long courseId, String title, String thumbnailUrl, String difficultyLevel,
                     double price, int lessonCount, Float duration,
                     List<VideoDTO> video, int commentCount, int enrolledUsers, String teacherName) {
        this.courseId = courseId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.difficultyLevel = difficultyLevel;
        this.price = price;
        this.lessonCount = lessonCount;
        this.duration = duration;
        this.videos = video;
        this.commentCount = commentCount;
        this.enrolledUsers = enrolledUsers;
        this.teacherName = teacherName;
    }
}
