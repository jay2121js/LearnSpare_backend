package com.example.SmartLearn.Repository;

import com.example.SmartLearn.DTO.VideoDTO;
import com.example.SmartLearn.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo  extends JpaRepository<Video,Long> {
    Video findVideoByVideoPublicId(String videoPublicId);
    void deleteByCourseCourseId(Long courseId);

}
