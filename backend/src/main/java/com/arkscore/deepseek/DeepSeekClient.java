package com.arkscore.deepseek;

public interface DeepSeekClient {

    String chat(String systemPrompt, String userPrompt);
}
