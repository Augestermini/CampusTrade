package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import com.second.hand.trading.server.dto.SuggestPriceRequest;
import com.second.hand.trading.server.dto.SuggestPriceResponse;
import com.second.hand.trading.server.service.AiService;
import com.second.hand.trading.server.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/generate-description")
    public ResultVo<GenerateDescriptionResponse> generateDescription(
            @RequestBody GenerateDescriptionRequest request) {
        return ResultVo.success(aiService.generateDescription(request));
    }

    @PostMapping("/suggest-price")
    public ResultVo<SuggestPriceResponse> suggestPrice(
            @RequestBody SuggestPriceRequest request) {
        return ResultVo.success(aiService.suggestPrice(request));
    }
}
