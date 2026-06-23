package com.second.hand.trading.server.service;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import com.second.hand.trading.server.dto.SuggestPriceRequest;
import com.second.hand.trading.server.dto.SuggestPriceResponse;

public interface AiService {

    GenerateDescriptionResponse generateDescription(GenerateDescriptionRequest request);

    SuggestPriceResponse suggestPrice(SuggestPriceRequest request);
}
