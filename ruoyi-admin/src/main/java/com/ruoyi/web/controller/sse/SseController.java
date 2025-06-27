package com.ruoyi.web.controller.sse;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.controller.sse.service.ChatService;
import com.ruoyi.web.controller.sse.service.LocalChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SseController {
    private final ChatService chatService;
    private final LocalChatService localChatService;

    public SseController(ChatService chatService, LocalChatService localChatService) {
        this.chatService = chatService;
        this.localChatService = localChatService;
    }

    @GetMapping("/stream")
    public SseEmitter streamChat(
            @RequestParam String question,
            @RequestParam(defaultValue = "deepseek") String mode) {
        SseEmitter emitter = new SseEmitter();
        if ("local".equals(mode)) {
            localChatService.streamChat(question, emitter);
        } else {
            chatService.streamChat(question, emitter);
        }
        return emitter;
    }
} 