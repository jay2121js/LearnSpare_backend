package com.example.SmartLearn.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

import com.cloudinary.Transformation;


@Service
public class CloudinarySerivce {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    private Cloudinary cloudinary;
    @Autowired
    public CloudinarySerivce(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    public String uploadImageFromUrl(String imageUrl) {
        try {
            Map uploadResult = cloudinary.uploader().upload(imageUrl, ObjectUtils.asMap(
                    "folder", "user-avatars",  // Optional: set folder
                    "overwrite", true
            ));
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or throw custom exception
        }
    }
    public String uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "video"));

            System.out.println("Uploaded Video URL: " + uploadResult.get("url"));
            System.out.println("Public ID: " + uploadResult.get("public_id"));

            return getMPEGDASH(uploadResult.get("public_id").toString());
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Cloudinary", e);
        }
    }

        public  String  getMPEGDASH(String videoId){
            return  "https://res.cloudinary.com/" + this.cloudName +
                    "/video/upload/f_auto:video/f_mpd/" + videoId + ".mpd";

        }

}