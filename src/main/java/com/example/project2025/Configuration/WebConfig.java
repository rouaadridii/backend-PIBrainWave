package com.example.project2025.Configuration;

import com.example.project2025.Entities.Tag;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = converter.getObjectMapper();

        // Register custom deserializer for Tag
        SimpleModule module = new SimpleModule();

        // Adding the custom deserializer for Tag
        module.addDeserializer(Tag.class, new JsonDeserializer<Tag>() {
            @Override
            public Tag deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                JsonNode node = p.getCodec().readTree(p);

                // Si c'est une simple chaîne comme "tag3"
                if (node.isTextual()) {
                    return new Tag(node.asText());
                }

                // Si c'est un objet JSON comme {"id": 1, "name": "tag3"}
                String name = node.get("name").asText();
                Tag tag = new Tag(name);

                if (node.has("id")) {
                    Long id = node.get("id").asLong();
                    tag.setId(id);
                }

                return tag;
            }
        });




        objectMapper.registerModule(module);

        // Support multiple content types
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.valueOf("application/json;charset=UTF-8"));
        converter.setSupportedMediaTypes(supportedMediaTypes);

        return converter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonConverter());
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // Map requests starting with /uploads/ to...
                .addResourceLocations("file:C:/temp/uploads/"); // ...the C:/temp/uploads/ directory
    }
}
