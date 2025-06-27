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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ChatService {
    private final VolcengineConfig config;
    private final LocalChatService localChatService;
    private ArkService arkService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChatService(VolcengineConfig config, LocalChatService localChatService) {
        this.config = config;
        this.localChatService = localChatService;
    }

    @PostConstruct
    public void init() {
        if ("api".equals(config.getMode())) {
            initArkService();
        }
    }

    private void initArkService() {
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
        executorService.shutdown();
    }

    public void streamChat(String prompt, SseEmitter emitter) {
        if ("local".equals(config.getMode())) {
            localChatService.streamChat(prompt, emitter);
            return;
        }

        executorService.execute(() -> {
            try {
                // 发送ready事件
                emitter.send(SseEmitter.event()
                    .name("ready")
                    .data("连接已就绪\n"));

                // 模拟思考时间
                Thread.sleep(1000);

                // 准备API请求
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(prompt)
                    .build());

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(config.getModel())
                    .messages(messages)
                    .build();

                // 收集API响应
                StringBuilder responseBuilder = new StringBuilder();

                // 发送API请求并处理响应
                arkService.streamChatCompletion(request)
                    .doOnError(throwable -> {
                        emitter.completeWithError(throwable);
                    })
                    .blockingForEach(delta -> {
                        if (!delta.getChoices().isEmpty()) {
                            String content = (String) delta.getChoices().get(0).getMessage().getContent();
                            if (content != null && !content.isEmpty()) {
                                responseBuilder.append(content);
                                // 将新内容按字符发送，每个字符后面加换行
                                char[] chars = content.toCharArray();
                                for (char c : chars) {
                                    emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data("{\"v\": \"" + c + "\"}\n"));
                                    Thread.sleep(100); // 模拟打字速度
                                }
                            }
                        }
                    });

                // 发送close事件
                emitter.send(SseEmitter.event()
                    .name("close")
                    .data("传输完成\n"));

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
    }
} 