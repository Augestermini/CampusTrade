package com.second.hand.trading.server.service;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;

public interface AiService {

    GenerateDescriptionResponse generateDescription(GenerateDescriptionRequest request);
}
