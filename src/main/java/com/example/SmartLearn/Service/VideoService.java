package com.example.SmartLearn.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.SmartLearn.DTO.VideoDTO;
import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.Video;
import com.example.SmartLearn.Repository.VideoRepo;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VideoService {
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private VideoRepo videoRepo;

    @Autowired
    private CourseService courseService;
    @Autowired
    private UtilServices utilServices;

    public Video getVideoById(Long id) {
        return videoRepo.findById(id).orElse(null);
    }
    @Transactional
    public boolean createVideo(MultipartFile videoFile, String title, String description, Long courseId) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        Course course = courseService.getCourseById(courseId);
        try{
            video.setCourse(course);
            uploadVideo(videoFile,video);
            course.setLessonCount(course.getLessonCount()+1);
            course.setDuration(video.getDuration()+course.getDuration());
            course.getVideos().add(video);
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

    }


    public void saveVideo(Video video) {
        videoRepo.save(video);
    }

    public boolean updateVideoDetails(String title,String description, Long videoId) {
        Video video = videoRepo.findById(videoId).orElse(null);
        if (video != null) {
            video.setTitle((title == null || title.isEmpty()) ? video.getTitle() : title);
            video.setDescription((description == null || description.isEmpty()) ? video.getDescription() : description);
            videoRepo.save(video);
            return true;
        }
        return false;
    }

    public List<Video> getAllVideo() {
                    return videoRepo.findAll();
            }

    public boolean updateVideo(MultipartFile videoFile, Long videoId) {
        try {
            Video video = videoRepo.findById(videoId).orElse(null);
            if (video == null) return false;
            String publicId = video.getVideoPublicId();
            uploadVideo(videoFile,video);
            videoRepo.save(video);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
            return true;
        } catch (IOException e) {
            log.error("Video update failed.", e);
            return false;
        }
    }

    public boolean deleteVideo(Long videoId) {
        try {
            Video video = videoRepo.findById(videoId).orElse(null);
            if (video == null) return false;
            cloudinary.uploader().destroy(video.getVideoPublicId(), ObjectUtils.asMap("resource_type", "video"));
            cloudinary.uploader().destroy(video.getNotesPublicId(), ObjectUtils.asMap("resource_type", "raw"));
            Course course = courseService.getByVideoId(videoId);
            course.setLessonCount(course.getLessonCount()-1);
            course.setDuration(course.getDuration()-video.getDuration());
            videoRepo.delete(video);
            courseService.sava(course);

            return true;
        } catch (IOException e) {
            log.error("Video deletion failed.", e);
            return false;
        }
    }

    public void uploadVideo(MultipartFile videoFile, Video video) throws IOException {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    videoFile.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            // Extract basic info
            String publicId = (String) uploadResult.get("public_id");
            String videoUrl = (String) uploadResult.get("secure_url");
            String hlsUrl = (String) uploadResult.get("playback_url"); // Available immediately
            Double duration = (Double) uploadResult.get("duration");   // Also available

            video.setVideoPublicId(publicId);
            video.setVideoUrl(videoUrl);
            video.setHlsUrl(hlsUrl);
            video.setDuration(duration != null ? duration.floatValue() : 0f);
            video.setHslReady(false); // Since HLS is already ready in this mode
            videoRepo.save(video);
            log.info("Video uploaded to Cloudinary, public_id: {}", uploadResult.get("public_id"));
        } catch (IOException e) {
            log.error("Failed to upload video to Cloudinary", e);
        }
    }

    public boolean uploadThumbnail(MultipartFile thumbnail, Course course) {
        try {
            Map upload = cloudinary.uploader().upload(
                    thumbnail.getBytes(), ObjectUtils.asMap(
                            "resource_type", "image"
                    )
            );
            course.setThumbnailUrl(upload.get("secure_url").toString());
            course.setThumbnailPublicId(upload.get("public_id").toString());
            courseService.sava(course);
            return true;
        } catch (IOException e) {
            log.error("Thumbnail upload failed.", e);
            return false;
        }
    }
    public boolean updateThumbnail(MultipartFile thumbnail, Course course) {
        try {
            String publicId = course.getThumbnailPublicId();
            if (uploadThumbnail(thumbnail, course)) {
                if (publicId != null && !publicId.isEmpty()){
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());}
            } }catch (Exception e) {
            log.error("Thumbnail update failed.", e);
            return false;
        }
        return true;
    }

    public void uploadNotesVideo(MultipartFile notesVideo, Video video) throws IOException {
        Map upload = cloudinary.uploader().upload(notesVideo.getBytes(), ObjectUtils.asMap("resource_type", "raw", "format", "pdf",
                "access_mode", "public"));
        video.setNotesPublicId(upload.get("public_id").toString());
        video.setVideoUrl(upload.get("secure_url").toString());
        saveVideo(video);
    }
    public void deleteNotesVideo(Video video) throws IOException {
        if (video.getNotesPublicId() != null) {
            cloudinary.uploader().destroy(video.getNotesPublicId(), ObjectUtils.asMap("resource_type", "raw"));
            video.setNotesPublicId(null);
            video.setVideoUrl(null);
        }
    }
    public void updateNotesVideo(MultipartFile notesVideo, Video video) throws IOException {
        cloudinary.uploader().destroy(video.getNotesPublicId(), ObjectUtils.asMap("resource_type", "raw"));
        uploadNotesVideo(notesVideo, video);
    }


    public Video getVideobyPublicId(String PublicId) {
       return videoRepo.findVideoByVideoPublicId(PublicId);
    }

    public void Embedvideos(String url,String title, String description, Long courseId){
            if (utilServices.checkUrl(url)){
                Course course = courseService.getCourseById(courseId);
                if (course == null) {
                    log.warn("Course not found for courseId: {}", courseId);
                    return;
                }
                Video video = new Video();
                video.setTitle(title);
                video.setDescription(description);
                video.setVideoUrl(url);
                video.setEmbedVideo(true);
                video.setCourse(course);
                videoRepo.save(video);
            }
    }
}
