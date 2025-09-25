package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 AI 管理能力
 */
@Component
public class AiMananger {

    @Resource
    private ClientV4 clientV4;

    // 稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 同步请求（答案较稳定）
     * @param systemMessages
     * @param userMessages
     * @return
     */
    public String doSyncStableRequest(String systemMessages, String userMessages) {
        return doRequest(systemMessages, userMessages, Boolean.FALSE, STABLE_TEMPERATURE);
    }

    /**
     * 同步请求（答案不那么稳定）
     * @param systemMessages
     * @param userMessages
     * @return
     */
    public String doSyncUnstableRequest(String systemMessages, String userMessages) {
        return doRequest(systemMessages, userMessages, Boolean.FALSE, UNSTABLE_TEMPERATURE);
    }
    /**
     * 同步请求
     * @param systemMessages
     * @param userMessages
     * @param temperature
     * @return
     */
    public String doSyncRequest(String systemMessages, String userMessages, Float temperature) {
        return doRequest(systemMessages, userMessages, Boolean.FALSE, temperature);
    }

    /**
     * 通用请求（简化请求传递）
     * @param systemMessages
     * @param userMessages
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(String systemMessages, String userMessages, Boolean stream, Float temperature) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessages);
        chatMessageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessages);
        chatMessageList.add(userChatMessage);
        return doRequest(chatMessageList, stream, temperature);
    }

    /**
     * 通用请求
     * @param messages
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 通用流式请求（简化请求传递）
     * @param systemMessages
     * @param userMessages
     * @param stream
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(String systemMessages, String userMessages, Float temperature) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessages);
        chatMessageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessages);
        chatMessageList.add(userChatMessage);
        return doStreamRequest(chatMessageList, temperature);
    }

    /**
     * 通用流式请求
     * @param messages
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getFlowable();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
