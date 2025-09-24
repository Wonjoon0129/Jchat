package top.kimwonjoon.domain.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TransportConfigStdio
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/24 16:49
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public  class TransportConfigStdio {

    private Map<String, Stdio> stdio;

    @Data
    public static class Stdio {
        private String command;
        private List<String> args;
    }
}