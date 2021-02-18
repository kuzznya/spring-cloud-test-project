package ru.intetech.mnemosynesystem.cloud.dummy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.intetech.mnemosynesystem.cloud.dummy.config.prop.GreetingProperties;

@Configuration
@EnableConfigurationProperties(GreetingProperties.class)
public class DummyConfig {
}
