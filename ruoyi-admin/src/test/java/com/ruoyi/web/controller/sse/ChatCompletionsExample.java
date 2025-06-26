package com.ruoyi.web.controller.sse;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.apache.commons.lang.StringUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
public class ChatCompletionsExample {
    static String apiKey = "8d6d4836-30fb-4a45-8930-7c2d1d44dc84";
    static ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
    static Dispatcher dispatcher = new Dispatcher();
    // The output time of the reasoning model is relatively long. Please increase the timeout period.
    static ArkService service = ArkService.builder().timeout(Duration.ofSeconds(1800)).connectTimeout(Duration.ofSeconds(20)).dispatcher(dispatcher).connectionPool(connectionPool).baseUrl("https://ark.cn-beijing.volces.com/api/v3").apiKey(apiKey).build();
    public static void main(String[] args) {
        System.out.println(" [Recommended]----- streaming request -----");
        final List<ChatMessage> streamMessages = new ArrayList<>();
        final ChatMessage streamUserMessage = ChatMessage.builder().role(ChatMessageRole.USER).content("常见的十字花科植物有哪些？").build();
        streamMessages.add(streamUserMessage);
        ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                .model("deepseek-r1-250120")
                .messages(streamMessages)
                .build();
        service.streamChatCompletion(streamChatCompletionRequest)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(
                        delta -> {
                            if (!delta.getChoices().isEmpty()) {
                                if (StringUtils.isNotEmpty(delta.getChoices().get(0).getMessage().getReasoningContent())) {
                                    System.out.print(delta.getChoices().get(0).getMessage().getReasoningContent());
                                } else {
                                    System.out.print(delta.getChoices().get(0).getMessage().getContent());
                                }
                            }
                        }
                );
        System.out.println("----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content("常见的十字花科植物有哪些？").build();
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("deepseek-r1-250120")
                .messages(messages)
                .build();
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(
                choice -> {
                    System.out.println(choice.getMessage().getReasoningContent());
                    System.out.println(choice.getMessage().getContent());
                }
        );
        // shutdown service after all requests is finished
        service.shutdownExecutor();
    }
}