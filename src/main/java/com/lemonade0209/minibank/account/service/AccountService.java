package com.lemonade0209.minibank.account.service;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.repository.AccountRepository;
import com.lemonade0209.minibank.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private long accountNumberSequence = 0L;

    private static final int MAX_RETRY_COUNT = 5;

    public Account openAccount(Long memberId) {
        if (memberRepository.findById(memberId) == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        for (int retryCount = 0; retryCount <= MAX_RETRY_COUNT; retryCount++) {
            String accountNumber = generateAccountNumber();

            if (accountRepository.findByAccountNumber(accountNumber) == null) {
                Account account = new Account(memberId, accountNumber);
                return accountRepository.save(account);
            }
        }
        throw new IllegalStateException("계좌번호 생성에 실패했습니다.");
    }

    private String generateAccountNumber() {
        return String.format("%012d", ++accountNumberSequence);
    }

    public Account findById(Long id) {
        return accountRepository.findById(id);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public List<Account> findByMemberId(Long memberId) {
        return accountRepository.findByMemberId(memberId);
    }

    public Account deposit(Long accountId, long amount) {
        Account account = accountRepository.findById(accountId);

        if (account == null) {
            throw new IllegalArgumentException("존재하지 않는 계좌입니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다.");
        }
        account.deposit(amount);
        return account;
    }
}
