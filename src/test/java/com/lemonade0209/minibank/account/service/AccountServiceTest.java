package com.lemonade0209.minibank.account.service;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.repository.MemoryAccountRepository;
import com.lemonade0209.minibank.member.domain.Member;
import com.lemonade0209.minibank.member.repository.MemoryMemberRepository;
import com.lemonade0209.minibank.transaction.repository.MemoryAccountTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.lemonade0209.minibank.transaction.domain.TransactionType.DEPOSIT;
import static com.lemonade0209.minibank.transaction.domain.TransactionType.WITHDRAWAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

    private AccountService accountService;
    private MemoryAccountRepository accountRepository;
    private MemoryMemberRepository memberRepository;
    private MemoryAccountTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new MemoryAccountRepository();
        memberRepository = new MemoryMemberRepository();
        transactionRepository = new MemoryAccountTransactionRepository();
        accountService = new AccountService(
                accountRepository,
                memberRepository,
                transactionRepository
        );
    }

    @Test
    void openAccount() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );

        // when
        Account account = accountService.openAccount(member.getId());

        // then
        assertThat(account.getId()).isNotNull();
        assertThat(account.getMemberId()).isEqualTo(member.getId());
        assertThat(account.getAccountNumber()).isEqualTo("000000000001");
        assertThat(account.getAccountNumber()).hasSize(12);
        assertThat(account.getBalance()).isZero();
        assertThat(accountService.findById(account.getId())).isSameAs(account);
    }

    @Test
    void retryWhenAccountNumberAlreadyExists() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );
        saveAccountsWithNumbers(1, 5);

        // when
        Account account = accountService.openAccount(member.getId());

        // then
        assertThat(account.getAccountNumber()).isEqualTo("000000000006");
    }

    @Test
    void failWhenAccountNumberRetryIsExhausted() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );
        saveAccountsWithNumbers(1, 6);

        // when & then
        assertThatThrownBy(() -> accountService.openAccount(member.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("계좌번호 생성에 실패했습니다.");
    }

    @Test
    void failWhenMemberDoesNotExist() {
        // when & then
        assertThatThrownBy(() -> accountService.openAccount(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
        assertThat(accountRepository.findAll()).isEmpty();
    }

    @Test
    void findAll() {
        // given
        Account account1 = new Account(1L, "100000000001");
        Account account2 = new Account(2L, "100000000002");
        accountRepository.save(account1);
        accountRepository.save(account2);

        // when
        var accounts = accountService.findAll();

        // then
        assertThat(accounts).containsExactlyInAnyOrder(account1, account2);
    }

    @Test
    void deposit() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );
        Account account = accountService.openAccount(member.getId());

        // when
        Account depositedAccount = accountService.deposit(account.getId(), 10_000L);

        // then
        assertThat(depositedAccount.getBalance()).isEqualTo(10_000L);
        assertThat(accountService.findById(account.getId()).getBalance()).isEqualTo(10_000L);
        assertThat(transactionRepository.findByAccountId(account.getId()))
                .singleElement()
                .satisfies(transaction -> {
                    assertThat(transaction.getType()).isEqualTo(DEPOSIT);
                    assertThat(transaction.getAmount()).isEqualTo(10_000L);
                    assertThat(transaction.getBalanceAfter()).isEqualTo(10_000L);
                    assertThat(transaction.getCounterpartyAccountId()).isNull();
                    assertThat(transaction.getTransferGroupId()).isNull();
                });
    }

    @Test
    void failWhenDepositAmountIsZero() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));

        // when & then
        assertThatThrownBy(() -> accountService.deposit(account.getId(), 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("입금액은 0보다 커야 합니다.");
        assertThat(account.getBalance()).isZero();
        assertThat(transactionRepository.findByAccountId(account.getId())).isEmpty();
    }

    @Test
    void failWhenDepositAmountIsNegative() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));

        // when & then
        assertThatThrownBy(() -> accountService.deposit(account.getId(), -1_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("입금액은 0보다 커야 합니다.");
        assertThat(account.getBalance()).isZero();
        assertThat(transactionRepository.findByAccountId(account.getId())).isEmpty();
    }

    @Test
    void withdraw() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));
        account.deposit(10_000L);

        // when
        boolean withdrawn = accountService.withdraw(account.getId(), 4_000L);

        // then
        assertThat(withdrawn).isTrue();
        assertThat(account.getBalance()).isEqualTo(6_000L);
        assertThat(transactionRepository.findByAccountId(account.getId()))
                .singleElement()
                .satisfies(transaction -> {
                    assertThat(transaction.getType()).isEqualTo(WITHDRAWAL);
                    assertThat(transaction.getAmount()).isEqualTo(4_000L);
                    assertThat(transaction.getBalanceAfter()).isEqualTo(6_000L);
                    assertThat(transaction.getCounterpartyAccountId()).isNull();
                    assertThat(transaction.getTransferGroupId()).isNull();
                });
    }

    @Test
    void failWhenWithdrawAmountIsZero() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));
        account.deposit(10_000L);

        // when & then
        assertThatThrownBy(() -> accountService.withdraw(account.getId(), 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출금액은 0보다 커야 합니다.");
        assertThat(account.getBalance()).isEqualTo(10_000L);
        assertThat(transactionRepository.findByAccountId(account.getId())).isEmpty();
    }

    @Test
    void failWhenWithdrawAmountIsNegative() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));
        account.deposit(10_000L);

        // when & then
        assertThatThrownBy(() -> accountService.withdraw(account.getId(), -1_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출금액은 0보다 커야 합니다.");
        assertThat(account.getBalance()).isEqualTo(10_000L);
        assertThat(transactionRepository.findByAccountId(account.getId())).isEmpty();
    }

    @Test
    void failWhenBalanceIsInsufficient() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));
        account.deposit(5_000L);

        // when
        boolean withdrawn = accountService.withdraw(account.getId(), 7_000L);

        // then
        assertThat(withdrawn).isFalse();
        assertThat(account.getBalance()).isEqualTo(5_000L);
        assertThat(transactionRepository.findByAccountId(account.getId())).isEmpty();
    }

    @Test
    void failWhenWithdrawAccountDoesNotExist() {
        // when & then
        assertThatThrownBy(() -> accountService.withdraw(999L, 1_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 계좌입니다.");
        assertThat(transactionRepository.findByAccountId(999L)).isEmpty();
    }

    private void saveAccountsWithNumbers(int start, int end) {
        for (int number = start; number <= end; number++) {
            String accountNumber = String.format("%012d", number);
            accountRepository.save(new Account(2L, accountNumber));
        }
    }
}
