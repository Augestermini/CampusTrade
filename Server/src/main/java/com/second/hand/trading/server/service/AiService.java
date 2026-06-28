package com.second.hand.trading.server.service;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import com.second.hand.trading.server.dto.SuggestPriceRequest;
import com.second.hand.trading.server.dto.SuggestPriceResponse;
import com.second.hand.trading.server.dto.TradeAdviceRequest;
import com.second.hand.trading.server.dto.TradeAdviceResponse;

public interface AiService {

    GenerateDescriptionResponse generateDescription(GenerateDescriptionRequest request);

    SuggestPriceResponse suggestPrice(SuggestPriceRequest request);

    TradeAdviceResponse tradeAdvice(TradeAdviceRequest request);
}
