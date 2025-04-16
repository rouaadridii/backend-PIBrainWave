package com.example.project2025.Controllers;
import com.example.project2025.Services.ReadLaterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/sse/notifications")
public class NotificationSSEController {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ReadLaterService readLaterService;

    public NotificationSSEController(ReadLaterService readLaterService) {
        this.readLaterService = readLaterService;
    }

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Set a large timeout
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((throwable) -> emitters.remove(userId));

        // Send a dummy event to keep the connection alive
        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
        } catch (IOException e) {
            emitters.remove(userId);
        }
        return emitter;
    }

    public void sendNotificationToUser(Long userId, String message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("readLaterNotification").data(Map.of("message", message)));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}