package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import com.second.hand.trading.server.dto.SuggestPriceRequest;
import com.second.hand.trading.server.dto.SuggestPriceResponse;
import com.second.hand.trading.server.service.AiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AiServiceImpl implements AiService {

    private static final BigDecimal MIN_RATE_LIMIT = new BigDecimal("0.05");
    private static final BigDecimal MAX_RATE_LIMIT = new BigDecimal("0.95");
    private static final BigDecimal TWO = new BigDecimal("2");

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

    @Override
    public SuggestPriceResponse suggestPrice(SuggestPriceRequest request) {
        // TODO: 后续可以接入大语言模型或历史成交数据模型，进一步优化价格建议逻辑。
        SuggestPriceResponse response = new SuggestPriceResponse();
        if (request == null
                || request.getOriginalPrice() == null
                || request.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            response.setPriceStatus("UNKNOWN");
            response.setReason("缺少商品原价，暂时无法生成准确的价格建议。");
            response.setWarning("缺少商品原价，暂时无法生成准确的价格建议。");
            return response;
        }

        BigDecimal[] rates = getConditionRates(request.getConditionLevel());
        adjustRatesByCategory(rates, request.getCategory());
        adjustRatesByUsedTime(rates, request.getUsedTime());
        normalizeRates(rates);

        BigDecimal originalPrice = request.getOriginalPrice();
        BigDecimal minPrice = calculatePrice(originalPrice, rates[0]);
        BigDecimal maxPrice = calculatePrice(originalPrice, rates[1]);
        BigDecimal suggestedPrice = minPrice.add(maxPrice)
                .divide(TWO, 2, RoundingMode.HALF_UP);

        response.setMinPrice(minPrice);
        response.setMaxPrice(maxPrice);
        response.setSuggestedPrice(suggestedPrice);
        response.setReason(buildPriceReason(request, rates));
        setPriceStatus(response, request.getPrice(), minPrice, maxPrice);
        return response;
    }

    private BigDecimal[] getConditionRates(String conditionLevel) {
        String condition = trimToEmpty(conditionLevel);
        if (containsAny(condition, "全新", "未拆封", "几乎全新")) {
            return rates("0.80", "0.90");
        }
        if (condition.contains("九成新")) {
            return rates("0.65", "0.75");
        }
        if (condition.contains("八成新")) {
            return rates("0.50", "0.65");
        }
        if (condition.contains("七成新")) {
            return rates("0.35", "0.50");
        }
        if (condition.contains("六成新")) {
            return rates("0.25", "0.35");
        }
        if (containsAny(condition, "五成新", "四成新", "三成新", "二成新", "一成新",
                "五成新及以下")) {
            return rates("0.10", "0.25");
        }
        return rates("0.40", "0.60");
    }

    private void adjustRatesByCategory(BigDecimal[] rates, String category) {
        String normalizedCategory = trimToEmpty(category);
        if ("教材书籍".equals(normalizedCategory)) {
            rates[1] = rates[1].subtract(new BigDecimal("0.10"));
        } else if ("宿舍用品".equals(normalizedCategory)
                || "生活用品".equals(normalizedCategory)) {
            subtractFromBothRates(rates, new BigDecimal("0.05"));
        }
    }

    private void adjustRatesByUsedTime(BigDecimal[] rates, String usedTime) {
        String normalizedUsedTime = trimToEmpty(usedTime);
        if (containsAny(normalizedUsedTime, "一个月", "1个月", "刚买", "几乎没用")) {
            rates[1] = rates[1].add(new BigDecimal("0.05"));
        } else if (containsAny(normalizedUsedTime, "三年", "3年", "更久")) {
            subtractFromBothRates(rates, new BigDecimal("0.15"));
        } else if (containsAny(normalizedUsedTime, "两年", "2年")) {
            subtractFromBothRates(rates, new BigDecimal("0.10"));
        } else if (containsAny(normalizedUsedTime, "一年", "1年")) {
            subtractFromBothRates(rates, new BigDecimal("0.05"));
        }
    }

    private void subtractFromBothRates(BigDecimal[] rates, BigDecimal adjustment) {
        rates[0] = rates[0].subtract(adjustment);
        rates[1] = rates[1].subtract(adjustment);
    }

    private void normalizeRates(BigDecimal[] rates) {
        rates[0] = clampRate(rates[0]);
        rates[1] = clampRate(rates[1]);
        if (rates[0].compareTo(rates[1]) > 0) {
            rates[0] = rates[1];
        }
    }

    private BigDecimal clampRate(BigDecimal rate) {
        if (rate.compareTo(MIN_RATE_LIMIT) < 0) {
            return MIN_RATE_LIMIT;
        }
        if (rate.compareTo(MAX_RATE_LIMIT) > 0) {
            return MAX_RATE_LIMIT;
        }
        return rate;
    }

    private BigDecimal calculatePrice(BigDecimal originalPrice, BigDecimal rate) {
        return originalPrice.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private void setPriceStatus(SuggestPriceResponse response, BigDecimal price,
                                BigDecimal minPrice, BigDecimal maxPrice) {
        if (price == null) {
            response.setPriceStatus("UNKNOWN");
            response.setWarning("缺少必要信息，暂时无法判断价格是否合理。");
        } else if (price.compareTo(minPrice) < 0) {
            response.setPriceStatus("TOO_LOW");
            response.setWarning("当前售价低于系统建议区间，可能导致卖家损失，也可能让买家认为商品存在异常。");
        } else if (price.compareTo(maxPrice) > 0) {
            response.setPriceStatus("TOO_HIGH");
            response.setWarning("当前售价高于系统建议区间，可能降低成交概率。");
        } else {
            response.setPriceStatus("NORMAL");
            response.setWarning("当前售价处于系统建议区间内，价格较为合理。");
        }
    }

    private String buildPriceReason(SuggestPriceRequest request, BigDecimal[] rates) {
        String category = defaultIfBlank(request.getCategory(), "其他分类");
        String condition = defaultIfBlank(request.getConditionLevel(), "成色未明确");
        String usedTime = trimToEmpty(request.getUsedTime());

        StringBuilder reason = new StringBuilder("该商品属于")
                .append(category)
                .append("，")
                .append(condition);
        if (!usedTime.isEmpty()) {
            reason.append("且使用时间约").append(usedTime);
        }
        reason.append("，结合成色、分类和使用时间，建议售价为原价的 ")
                .append(toPercent(rates[0]))
                .append("% - ")
                .append(toPercent(rates[1]))
                .append("%。");

        if ("数码产品".equals(trimToEmpty(request.getCategory()))) {
            reason.append("数码产品还需结合电池、屏幕和功能情况综合判断。");
        }
        return reason.toString();
    }

    private String toPercent(BigDecimal rate) {
        return rate.multiply(new BigDecimal("100"))
                .stripTrailingZeros()
                .toPlainString();
    }

    private BigDecimal[] rates(String minRate, String maxRate) {
        return new BigDecimal[]{new BigDecimal(minRate), new BigDecimal(maxRate)};
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
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
