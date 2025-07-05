package com.example.SmartLearn.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.SmartLearn.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarUploadService {

    private final Cloudinary cloudinary;
    private final UserRepo userRepo;

    /**
     * Asynchronously downloads the original Google URL and re-uploads it to Cloudinary.
     * Once done, updates the user's avatar field in the database.
     *
     * @param userId     The DB ID of the user.
     * @param pictureUrl The original Google avatar URL.
     */
    @Async
    @Transactional
    public void uploadAvatarAsync(Long userId, String pictureUrl) {
        try {
            // 1. Upload via Cloudinary SDK
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    pictureUrl,
                    ObjectUtils.asMap(
                            "folder", "user-avatars",
                            "overwrite", true
                    )
            );

            String secureUrl = result.get("secure_url").toString();
            log.info("Cloudinary upload complete for user {}: {}", userId, secureUrl);

            // 2. Persist the CDN URL back to the user
            userRepo.findById(userId).ifPresent(user -> {
                user.setAvatar(secureUrl);
                userRepo.save(user);
                log.info("User {} avatar updated in DB", userId);
            });

        } catch (Exception e) {
            log.error("Async avatar upload failed for user {}", userId, e);
        }
    }
}
