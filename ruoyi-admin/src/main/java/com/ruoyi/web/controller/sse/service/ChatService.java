package com.ruoyi.web.controller.sse.service;

import com.ruoyi.web.controller.sse.config.VolcengineConfig;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChatService {
    private final VolcengineConfig config;
    private ArkService arkService;

    public ChatService(VolcengineConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        ConnectionPool connectionPool = new ConnectionPool(
            config.getConnectionPoolSize(),
            config.getConnectionPoolKeepAlive(),
            TimeUnit.SECONDS
        );
        Dispatcher dispatcher = new Dispatcher();

        arkService = ArkService.builder()
            .timeout(Duration.ofSeconds(config.getTimeout()))
            .connectTimeout(Duration.ofSeconds(config.getConnectTimeout()))
            .dispatcher(dispatcher)
            .connectionPool(connectionPool)
            .baseUrl(config.getBaseUrl())
            .apiKey(config.getApiKey())
            .build();
    }

    @PreDestroy
    public void destroy() {
        if (arkService != null) {
            arkService.shutdownExecutor();
        }
    }

    public void streamChat(String prompt, SseEmitter emitter) {
        try {
            // 发送ready事件
            emitter.send(SseEmitter.event()
                .name("ready")
                .data("连接已就绪"));

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(prompt)
                .build());

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(config.getModel())
                .messages(messages)
                .build();

            arkService.streamChatCompletion(request)
                .doOnError(throwable -> {
                    emitter.completeWithError(throwable);
                })
                .blockingForEach(delta -> {
                    if (!delta.getChoices().isEmpty()) {
                        String content = (String) delta.getChoices().get(0).getMessage().getContent();
                        if (content != null && !content.isEmpty()) {
                            // 发送消息事件，使用JSON格式
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data("{\"v\": \"" + content.replace("\"", "\\\"") + "\"}"));
                        }
                    }
                });

            // 发送close事件
            emitter.send(SseEmitter.event()
                .name("close")
                .data("传输完成"));

            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

} 