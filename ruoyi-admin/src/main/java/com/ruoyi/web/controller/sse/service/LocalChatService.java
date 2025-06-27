package com.ruoyi.web.controller.sse.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LocalChatService {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }

    public void streamChat(String prompt, SseEmitter emitter) {
        executorService.execute(() -> {
            try {
                // 发送ready事件
                emitter.send(SseEmitter.event()
                    .name("ready")
                    .data("连接已就绪"));

                // 模拟思考时间
                Thread.sleep(1000);

                // 获取多行响应
                String[] lines = generateLocalResponse(prompt).split("\n");

                // 逐行发送响应
                for (String line : lines) {
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"v\": \"" + line + "\\n\"}"));
                    Thread.sleep(500); // 每行之间间隔500ms
                }

                // 发送close事件
                emitter.send(SseEmitter.event()
                    .name("close")
                    .data("传输完成"));

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
    }

    private String generateLocalResponse(String prompt) {
        // 这里返回多行文本
        if (prompt.contains("你好") || prompt.contains("hi") || prompt.contains("hello")) {
            return "你好！\n" +
                   "我是本地模拟的AI助手，\n" +
                   "很高兴为你服务。";
        } else if (prompt.contains("天气")) {
            return "抱歉，作为本地模拟助手，\n" +
                   "我无法获取实时天气信息。\n" +
                   "但我建议你可以看看天气预报APP或网站。";
        } else if (prompt.contains("时间") || prompt.contains("几点")) {
            return "我是本地模拟助手，\n" +
                   "虽然不能获取实时时间，\n" +
                   "但我建议你看看电脑右下角的时间显示哦！";
        } else {
            return "这是一个本地模拟的回答。\n" +
                   "我的功能有限，\n" +
                   "仅作为测试和演示使用。\n" +
                   "如果你需要更专业的回答，\n" +
                   "请切换到API模式。";
        }
    }
} 