package com.lemonade0209.minibank.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projectStatus", "애플리케이션 정상 작동 중");
        return "home";
    }
}
