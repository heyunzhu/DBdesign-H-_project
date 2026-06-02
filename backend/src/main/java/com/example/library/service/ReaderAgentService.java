package com.example.library.service;

import com.example.library.dto.AgentChatRequest;
import com.example.library.vo.AgentChatResponse;

public interface ReaderAgentService {

    AgentChatResponse chat(AgentChatRequest request);
}
