package com.lemonade0209.minibank.web;

import com.lemonade0209.minibank.member.domain.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/members/add")
    public String addForm() {
        log.info("GET /members/add handler called");
        return "members/add";
    }

    @PostMapping("/members/add")
    public String addMember(@ModelAttribute Member member) {
        log.info("loginId={}, name={}", member.getLoginId(), member.getName());
        return "members/add";
    }

    @GetMapping("/login")
    public String loginForm() {
        log.info("GET /login handler called");
        return "login";
    }
}
