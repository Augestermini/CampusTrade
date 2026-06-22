package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import com.second.hand.trading.server.service.AiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AiServiceImpl implements AiService {

    @Override
    public GenerateDescriptionResponse generateDescription(GenerateDescriptionRequest request) {
        // TODO: 后续可以在这里接入 DeepSeek、OpenAI、通义千问等大模型 API，替换当前的模板生成逻辑。
        if (request == null) {
            request = new GenerateDescriptionRequest();
        }

        String name = defaultIfBlank(request.getName(), "二手商品");
        String conditionLevel = trimToEmpty(request.getConditionLevel());
        String usedTime = trimToEmpty(request.getUsedTime());
        String accessories = trimToEmpty(request.getAccessories());

        StringBuilder description = new StringBuilder("这是一件");
        if (!conditionLevel.isEmpty()) {
            description.append(conditionLevel).append("的");
        }
        description.append(" ").append(name);

        if (!usedTime.isEmpty()) {
            description.append("，已使用约").append(usedTime);
        }
        description.append("，外观保存良好，功能正常。");

        if (!accessories.isEmpty()) {
            description.append("商品包含").append(accessories).append("，");
        }
        description.append(getUsageScenario(request.getCategory())).append("。");

        BigDecimal price = request.getPrice();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            description.append("目前售价为 ")
                    .append(price.stripTrailingZeros().toPlainString())
                    .append(" 元，");
        }

        if (Boolean.TRUE.equals(request.getCampusTrade())) {
            description.append("支持校内线下面交，有意者可以留言联系。");
        } else if (description.charAt(description.length() - 1) == '，') {
            description.setCharAt(description.length() - 1, '。');
        }

        return new GenerateDescriptionResponse(description.toString());
    }

    private String getUsageScenario(String category) {
        String normalizedCategory = trimToEmpty(category);
        switch (normalizedCategory) {
            case "数码产品":
                return "适合学习、办公、网课和日常娱乐";
            case "教材书籍":
                return "适合课程学习、复习备考和资料查阅";
            case "宿舍用品":
                return "适合宿舍日常使用";
            case "生活用品":
                return "适合校园生活日常使用";
            case "运动用品":
                return "适合运动锻炼和课余活动";
            default:
                return "适合校园二手交易使用";
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmedValue = trimToEmpty(value);
        return trimmedValue.isEmpty() ? defaultValue : trimmedValue;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
