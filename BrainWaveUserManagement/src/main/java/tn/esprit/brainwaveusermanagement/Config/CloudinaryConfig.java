package tn.esprit.brainwaveusermanagement.Config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    private Dotenv dotenv = Dotenv.load();

    private String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
    private String apiKey = dotenv.get("CLOUDINARY_API_KEY");
    private String apiSecret = dotenv.get("CLOUDINARY_API_SECRET");

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}
