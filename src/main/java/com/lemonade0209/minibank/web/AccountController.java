package com.lemonade0209.minibank.web;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public String accounts(@RequestParam Long memberId, Model model) {
        model.addAttribute("accounts", accountService.findByMemberId(memberId));
        model.addAttribute("memberId", memberId);
        return "accounts/accounts";
    }

    @GetMapping("/accounts/{accountId}")
    public String account(@PathVariable Long accountId, Model model) {
        Account account = accountService.findById(accountId);
        model.addAttribute("account", account);
        return "accounts/account";
    }

    @GetMapping("/accounts/add")
    public String addForm(@ModelAttribute Account account) {
        return "accounts/add";
    }

    @PostMapping("/accounts/add")
    public String addAccount(@ModelAttribute Account account,
                             RedirectAttributes redirectAttributes) {
        Account openedAccount = accountService.openAccount(account.getMemberId());
        redirectAttributes.addAttribute("memberId", openedAccount.getMemberId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/accounts";
    }
}
