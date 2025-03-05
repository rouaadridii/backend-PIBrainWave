package tn.esprit.brainwaveusermanagement.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, String> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url");
    }

    public void deleteImage(String imageUrl,String cv,String diploma) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        if (cv == null || cv.isEmpty()) {
            return;
        }
        if (diploma == null || diploma.isEmpty()) {
            return;
        }
        String publicId = extractPublicId(imageUrl);
        String publicId2 = extractPublicId(cv);
        String publicId3 = extractPublicId(diploma);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        cloudinary.uploader().destroy(publicId2, ObjectUtils.emptyMap());
        cloudinary.uploader().destroy(publicId3, ObjectUtils.emptyMap());
    }

    private String extractPublicId(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
    }
}
