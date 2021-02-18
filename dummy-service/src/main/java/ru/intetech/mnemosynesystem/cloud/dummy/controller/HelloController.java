package ru.intetech.mnemosynesystem.cloud.dummy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.intetech.mnemosynesystem.cloud.dummy.config.prop.GreetingProperties;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final GreetingProperties properties;

    @GetMapping("/hello")
    public String getHello() {
        return properties.getGreeting();
    }
}
