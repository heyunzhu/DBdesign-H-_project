package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.AgentChatRequest;
import com.example.library.service.ReaderAgentService;
import com.example.library.vo.AgentChatResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader-agent")
public class ReaderAgentController {

    private final ReaderAgentService readerAgentService;

    public ReaderAgentController(ReaderAgentService readerAgentService) {
        this.readerAgentService = readerAgentService;
    }

    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        return ApiResponse.success(readerAgentService.chat(request));
    }
}
