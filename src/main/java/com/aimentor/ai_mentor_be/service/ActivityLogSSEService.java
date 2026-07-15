package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.ActivityLogResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ActivityLogSSEService {

    private final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {

        SseEmitter emitter = new SseEmitter(0L);

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    public void send(ActivityLogResponse response) {

        for (SseEmitter emitter : emitters) {

            try {
                emitter.send(
                        SseEmitter.event()
                                .name("activity")
                                .data(response)
                );
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }

        }
    }
}