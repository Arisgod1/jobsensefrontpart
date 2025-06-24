package com.tmd.ai.config;

import com.tmd.ai.constants.SystemConstants;
import com.tmd.ai.model.AlibabaOpenAiChatModel;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel model,ChatMemory chatMemory){
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.CHAT_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)

                )
                .build();
    }
    @Bean
    public ChatClient PDFModel(OpenAiChatModel model, ChatMemory chatMemory){
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.PDF_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)

                )
                .build();
    }
    @Bean
    public ChatClient pdfChatClient(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient
                .builder(model)
                .defaultSystem("请根据上下文回答问题，遇到上下文没有的问题，不要随意编造。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(
                                vectorStore,
                                SearchRequest.builder()
                                        .similarityThreshold(0.6)
                                        .topK(2)
                                        .build()
                        )
                )
                .build();
    }

    @Bean
    public AlibabaOpenAiChatModel alibabaOpenAiChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention) {
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
        String projectId = StringUtils.hasText(chatProperties.getProjectId()) ? chatProperties.getProjectId() : commonProperties.getProjectId();
        String organizationId = StringUtils.hasText(chatProperties.getOrganizationId()) ? chatProperties.getOrganizationId() : commonProperties.getOrganizationId();
        Map<String, List<String>> connectionHeaders = new HashMap<>();
        if (StringUtils.hasText(projectId)) {
            connectionHeaders.put("OpenAI-Project", List.of(projectId));
        }

        if (StringUtils.hasText(organizationId)) {
            connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
        }
        RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(new SimpleApiKey(apiKey)).headers(CollectionUtils.toMultiValueMap(connectionHeaders)).completionsPath(chatProperties.getCompletionsPath()).embeddingsPath("/v1/embeddings").restClientBuilder(restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();
        AlibabaOpenAiChatModel chatModel = AlibabaOpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatProperties.getOptions()).toolCallingManager(toolCallingManager).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry) observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }
}
