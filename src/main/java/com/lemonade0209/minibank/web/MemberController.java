package com.lemonade0209.minibank.web;

import com.lemonade0209.minibank.member.domain.Member;
import com.lemonade0209.minibank.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MemberService memberService;

    @GetMapping("/members/add")
    public String addForm(@ModelAttribute("member") Member member) {
        log.info("GET /members/add handler called");
        return "members/add";
    }

    @PostMapping("/members/add")
    public String addMember(@ModelAttribute Member member,
                            RedirectAttributes redirectAttributes) {
        log.info("loginId={}, name={}", member.getLoginId(), member.getName());
        Member joinedMember = memberService.join(member);
        redirectAttributes.addAttribute("memberId", joinedMember.getId());
        redirectAttributes.addAttribute("joined", true);
        return "redirect:/accounts/add";
    }

    @GetMapping("/login")
    public String loginForm() {
        log.info("GET /login handler called");
        return "login";
    }
}
