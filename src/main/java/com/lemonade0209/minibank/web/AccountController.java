package com.lemonade0209.minibank.web;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
        accountService.save(new Account(1L, "000000000001"));
        accountService.save(new Account(1L, "000000000002"));
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        return "accounts/accounts";
    }

    @GetMapping("/accounts/{accountId}")
    public String account(@PathVariable Long accountId, Model model) {
        Account account = accountService.findById(accountId);
        model.addAttribute("account", account);
        return "accounts/account";
    }
}
