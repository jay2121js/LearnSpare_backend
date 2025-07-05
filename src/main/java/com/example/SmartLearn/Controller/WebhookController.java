package com.example.SmartLearn.Controller;

import com.example.SmartLearn.Entity.Video;
import com.example.SmartLearn.Service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController
{
    @Autowired
    private VideoService videoService;

    @PostMapping("/cloudinary")
    public ResponseEntity<String> handleCloudinaryWebhook(@RequestBody Map<String, Object> payload){
        String publicId = (String) payload.get("public_id");
        Video video = videoService.getVideobyPublicId(publicId);
        if (video != null){
            video.setHslReady(true);
            videoService.saveVideo(video);
        }
        return ResponseEntity.ok("Webhook received");
    }
}
