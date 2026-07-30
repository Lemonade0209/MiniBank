package com.lemonade0209.minibank.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthController {
    @ResponseBody
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
