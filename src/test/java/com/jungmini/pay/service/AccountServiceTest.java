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

import java.util.List;
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

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SIZE_EXCEED);
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

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
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
        assertThat(savedTransaction.getRemitterBalanceSnapshot()).isEqualTo(remitterBalance);
        assertThat(savedTransaction.getRecipientBalanceSnapshot()).isEqualTo(recipientBalance);
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

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.NOT_FRIENDS);
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

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
        assertThat(transaction.getTransactionResultType()).isNull();
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(remitterAccount.getBalance()).isEqualTo(remitterBalance);
        assertThat(recipientAccount.getBalance()).isEqualTo(recipientBalance);
    }

    @Test
    @DisplayName("계좌 상세 조회 성공")
    void get_account_info_success() {
        Account account = AccountFactory.account();
        Member owner = account.getOwner();

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(account));

        Account findedAccount = accountService.getAccountInfo(account.getAccountNumber(), owner);
        assertThat(findedAccount).isEqualTo(account);
    }

    @Test
    @DisplayName("계좌 상세 조회 실패 - 계좌 조회 못찾음")
    void get_account_info_fail_account_not_found() {
        Account account = AccountFactory.account();
        Member owner = account.getOwner();

        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class,
                () -> accountService.getAccountInfo(account.getAccountNumber(), owner));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getDescription());
    }

    @Test
    @DisplayName("계좌 상세 조회 실패 - 본인 계좌가 아닌 정보 요청")
    void get_account_info_fail_requester_is_not_owner() {
        Account account = AccountFactory.account();
        Member member = MemberFactory.memberFrom("dhasdfjkashdhf@naver.com");

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(account));

        PayException payException = assertThrows(PayException.class,
                () -> accountService.getAccountInfo(account.getAccountNumber(), member));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.REQUESTER_IS_NOT_OWNER);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription());
    }

    @Test
    @DisplayName("거래 내역 리스트 조회 성공")
    void get_transactions_success() {
        Account account = AccountFactory.account();
        Member owner = account.getOwner();

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findAllByRecipientAccountOrRemitterAccountAndTransactionResultTypeOrderByCreatedAtDesc(any(), any(), any(), any()))
                .thenReturn(List.of(TransactionFactory.from(), TransactionFactory.from()));

        List<Transaction> transactions = accountService.getTransactions(account.getAccountNumber(), owner, any());

        assertThat(transactions.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("거래 내역 리스트 실패 - 계좌를 못 찾은 경우")
    void get_transactions_fail_account_not_found() {
        Account account = AccountFactory.account();
        Member notOwner = MemberFactory.memberFrom("notOwner@test.com");

        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class,
                () -> accountService.getTransactions(account.getAccountNumber(), notOwner, any()));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getDescription());
    }

    @Test
    @DisplayName("거래 내역 리스트 실패 - 계좌 소유주 아닌 경우")
    void get_transactions_fail_requester_not_owner() {
        Account account = AccountFactory.account();
        Member notOwner = MemberFactory.memberFrom("notOwner@test.com");

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(account));

        PayException payException = assertThrows(PayException.class,
                () -> accountService.getTransactions(account.getAccountNumber(), notOwner, any()));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.REQUESTER_IS_NOT_OWNER);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription());
    }
}