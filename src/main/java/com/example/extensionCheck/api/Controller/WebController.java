package com.example.extensionCheck.api.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html 을 렌더링
    }
}