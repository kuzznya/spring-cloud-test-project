package ru.intetech.mnemosynesystem.cloud.dummy.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("dummy")
@Data
public class GreetingProperties {
    private String greeting = "hello";
}
