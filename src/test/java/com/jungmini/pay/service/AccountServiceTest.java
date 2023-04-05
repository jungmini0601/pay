package com.jungmini.pay.service;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.domain.Transaction;
import com.jungmini.pay.domain.type.AccountStatus;
import com.jungmini.pay.domain.type.TransactionResultType;
import com.jungmini.pay.domain.type.TransactionType;
import com.jungmini.pay.fixture.AccountFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.fixture.TransactionFactory;
import com.jungmini.pay.repository.AccountRepository;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.jungmini.pay.domain.Account.MAX_ACCOUNT_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private TransactionRepository transactionRepository;


    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void create_account_success() {
        Member owner = MemberFactory.member();
        Account account = AccountFactory.account();

        given(accountRepository.findFirstByOrderByCreatedAtDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(account);

        Account createdAccount = accountService.createAccount(owner);

        assertThat(createdAccount.getAccountNumber()).isEqualTo(Account.DEFAULT_ACCOUNT_NUMBER);
        assertThat(createdAccount.getBalance()).isEqualTo(0);
        assertThat(createdAccount.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);
        assertThat(createdAccount.getOwner().getEmail()).isEqualTo(owner.getEmail());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 최대 계좌 생성 개수 초과")
    void create_account_fail_account_size_exceed() {
        Member owner = MemberFactory.member();

        given(accountRepository.countByOwner(any()))
                .willReturn(MAX_ACCOUNT_SIZE);

        PayException payException = assertThrows(PayException.class, () -> {
            accountService.createAccount(owner);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SIZE_EXCEED.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_SIZE_EXCEED.getDescription());
    }

    @Test
    @DisplayName("계좌 충전 성공")
    void charge_point_success() {
        Member owner = MemberFactory.member();
        Account account = AccountFactory.accountFrom(owner);
        int amount = 100000;

        given(accountRepository.findById(any()))
                .willReturn(Optional.of(account));

        Account processedAccount = accountService.chargePoint(amount, account, owner);

        assertThat(processedAccount.getBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("계좌 충전 실패 - 계좌 못 찾은 경우")
    void charge_point_fail_account_not_found() {
        Member owner = MemberFactory.member();
        Account account = AccountFactory.accountFrom(owner);
        int amount = 100000;

        given(accountRepository.findById(any()))
                .willReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class, () -> {
            accountService.chargePoint(amount, account, owner);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getDescription());
    }

    @Test
    @DisplayName("송금 성공")
    void remit_success() {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Member recipient = MemberFactory.memberFrom("recipient@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, remitterBalance);
        Account recipientAccount = AccountFactory.accountFromOwnerAndBalance(recipient, recipientBalance);
        Transaction transaction = TransactionFactory
                .transactionRequest(remitterAccount, recipientAccount, amount);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(recipientAccount))
                .thenReturn(Optional.of(remitterAccount));

        when(friendRepository.existsFriendByRecipientAndRequester(any(), any()))
                .thenReturn(true)
                .thenReturn(false);

        when(transactionRepository.save(any()))
                .thenReturn(transaction);

        Transaction savedTransaction = accountService.remit(transaction, remitter);

        assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.REMIT);
        assertThat(savedTransaction.getTransactionResultType()).isEqualTo(TransactionResultType.SUCCESS);
        assertThat(savedTransaction.getRemitterAccount()).isEqualTo(remitterAccount);
        assertThat(savedTransaction.getBalanceSnapshot()).isEqualTo(remitterBalance);
        assertThat(savedTransaction.getAmount()).isEqualTo(amount);
        assertThat(savedTransaction.getRecipientAccount().getBalance()).isEqualTo(recipientBalance + amount);
        assertThat(savedTransaction.getRemitterAccount().getBalance()).isEqualTo(remitterBalance - amount);
    }

    @Test
    @DisplayName("송금 실패 - 친구가 아닌 관계 송금 실패 정보를 저장")
    void when_remit_fail_transaction_fail_saved() {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Member recipient = MemberFactory.memberFrom("recipient@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, remitterBalance);
        Account recipientAccount = AccountFactory.accountFromOwnerAndBalance(recipient, recipientBalance);
        Transaction transaction = TransactionFactory
                .transactionRequest(remitterAccount, recipientAccount, amount);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(recipientAccount))
                .thenReturn(Optional.of(remitterAccount))
                .thenReturn(Optional.of(recipientAccount))
                .thenReturn(Optional.of(remitterAccount));

        when(friendRepository.existsFriendByRecipientAndRequester(any(), any()))
                .thenReturn(false)
                .thenReturn(false);

        PayException payException = assertThrows(PayException.class,
                () -> accountService.remit(transaction, remitter));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.NOT_FRIENDS.toString());
        assertThat(transaction.getTransactionResultType()).isEqualTo(TransactionResultType.FAIL);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(remitterAccount.getBalance()).isEqualTo(remitterBalance);
        assertThat(recipientAccount.getBalance()).isEqualTo(recipientBalance);
    }

    @Test
    @DisplayName("송금 실패 - 친구가 계좌 못 찾음 정보 저장 X")
    void when_remit_fail_account_not_found() {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Member recipient = MemberFactory.memberFrom("recipient@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, remitterBalance);
        Account recipientAccount = AccountFactory.accountFromOwnerAndBalance(recipient, recipientBalance);
        Transaction transaction = TransactionFactory
                .transactionRequest(remitterAccount, recipientAccount, amount);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());


        PayException payException = assertThrows(PayException.class,
                () -> accountService.remit(transaction, remitter));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.toString());
        assertThat(transaction.getTransactionResultType()).isNull();
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(remitterAccount.getBalance()).isEqualTo(remitterBalance);
        assertThat(recipientAccount.getBalance()).isEqualTo(recipientBalance);
    }
}