package com.ruoyi.web.controller.sse;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.controller.sse.service.ChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SseController {
    private final ChatService chatService;

    public SseController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/stream")
    public SseEmitter streamChat(@RequestParam String question) {
        SseEmitter emitter = new SseEmitter();
        chatService.streamChat(question, emitter);
        return emitter;
    }
} 