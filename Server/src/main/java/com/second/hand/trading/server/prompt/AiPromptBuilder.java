package com.second.hand.trading.server.prompt;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.TradeAdviceRequest;
import org.springframework.stereotype.Component;

@Component
public class AiPromptBuilder {

    public String descriptionSystemPrompt() {
        return "你是一个校园二手交易商品描述生成助手。";
    }

    public String buildDescriptionPrompt(GenerateDescriptionRequest request) {
        GenerateDescriptionRequest safeRequest =
                request == null ? new GenerateDescriptionRequest() : request;
        return "请根据以下商品信息生成一段自然、真实、简洁的中文商品描述。\n"
                + "要求：\n"
                + "1. 不要夸大商品情况。\n"
                + "2. 不要编造未提供的信息。\n"
                + "3. 适合校园二手交易场景。\n"
                + "4. 如果支持校内交易，请在结尾说明支持校内线下面交。\n"
                + "5. 只输出商品描述文本，不要输出 Markdown。\n"
                + "商品名称：" + value(safeRequest.getName()) + "\n"
                + "商品分类：" + value(safeRequest.getCategory()) + "\n"
                + "成色：" + value(safeRequest.getConditionLevel()) + "\n"
                + "使用时间：" + value(safeRequest.getUsedTime()) + "\n"
                + "配件：" + value(safeRequest.getAccessories()) + "\n"
                + "售价：" + value(safeRequest.getPrice()) + "\n"
                + "是否支持校内交易：" + value(safeRequest.getCampusTrade());
    }

    public String tradeAdviceSystemPrompt() {
        return "你是一个校园二手交易安全助手。";
    }

    public String buildTradeAdvicePrompt(TradeAdviceRequest request) {
        TradeAdviceRequest safeRequest =
                request == null ? new TradeAdviceRequest() : request;
        return "请根据以下商品信息，为买家生成购买建议。\n"
                + "要求：\n"
                + "1. 必须只输出 JSON，不要输出 Markdown，不要输出解释性文字。\n"
                + "2. JSON 字段必须包括 advice、checkItems、questions、safetyTips、riskLevel。\n"
                + "3. checkItems、questions、safetyTips 必须是字符串数组。\n"
                + "4. riskLevel 只能是 LOW、MEDIUM、HIGH。\n"
                + "5. 建议内容要适合校园二手交易场景。\n"
                + "6. 如果描述中出现先付款、先转账、定金、微信转账、不见面、发货、外校、不退不换、急出秒付等内容，应提高风险等级。\n"
                + "7. 如果价格明显低于原价，也要提醒核实商品真实性。\n"
                + "8. 建议优先选择图书馆、教学楼大厅、食堂门口等校内公共地点面交。\n"
                + "商品信息：\n"
                + "商品名称：" + value(safeRequest.getProductName()) + "\n"
                + "商品分类：" + value(safeRequest.getCategory()) + "\n"
                + "商品描述：" + value(safeRequest.getDescription()) + "\n"
                + "当前售价：" + value(safeRequest.getPrice()) + "\n"
                + "商品原价：" + value(safeRequest.getOriginalPrice()) + "\n"
                + "成色：" + value(safeRequest.getConditionLevel()) + "\n"
                + "使用时间：" + value(safeRequest.getUsedTime()) + "\n"
                + "用户角色：" + value(safeRequest.getUserRole()) + "\n"
                + "买家当前问题：" + value(safeRequest.getUserQuestion()) + "\n"
                + "请优先直接回答买家的当前问题，并结合商品信息给出具体建议。\n"
                + "输出 JSON 格式："
                + "{\"advice\":\"综合购买建议\",\"checkItems\":[\"检查项1\"],"
                + "\"questions\":[\"建议问题1\"],\"safetyTips\":[\"安全提醒1\"],"
                + "\"riskLevel\":\"LOW\"}";
    }

    private String value(Object value) {
        return value == null || value.toString().trim().isEmpty()
                ? "未提供"
                : value.toString().trim();
    }
}
