package com.example.SmartLearn.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data

@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;
    String courseName;
    @Column(nullable = false)
    private String category;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String thumbnailPublicId;
    private int lessonCount;
    private Float duration;
    private int commentCount;
    private String difficultyLevel;
    private double price;
    private int enrolledUsers;
    private String teacherName;
    @ManyToMany(mappedBy = "courses", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<Student> students = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "teacherId")
    @JsonBackReference
    private Teacher instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos;


    private Date createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.students = (this.students == null) ? new ArrayList<>() : this.students;
        this.videos = (this.videos == null) ? new ArrayList<>() : this.videos;
        this.enrolledUsers = students.size(); // Set count here too
        this.duration = Float.parseFloat("0");
        this.lessonCount = this.students.size();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lessonCount = this.videos.size();
        this.enrolledUsers = (students != null) ? students.size() : 0;
    }


}
