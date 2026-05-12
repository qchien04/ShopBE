package com.controller.AI;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiRequest {
    private String q;
    private List<Map<String, String>> history;
}
