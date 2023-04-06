package com.jungmini.pay.web;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.domain.Transaction;

import com.jungmini.pay.common.resolover.SigninMember;
import com.jungmini.pay.web.dto.AccountDTO;
import com.jungmini.pay.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<AccountDTO.CreateAccountResponse> createAccount(
            @SigninMember Member member) {

        Account newAccount = accountService.createAccount(member);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountDTO.CreateAccountResponse.from(newAccount));
    }

    @PostMapping("/accounts/points")
    public ResponseEntity<AccountDTO.ChargePointResponse> chargePoint(
            @RequestBody @Valid AccountDTO.ChargePointRequest chargePointRequest,
            @SigninMember Member member) {

        Account account = accountService
                .chargePoint(chargePointRequest.getAmount(), chargePointRequest, member);

        return ResponseEntity.status(HttpStatus.OK)
                .body(AccountDTO.ChargePointResponse.from(account));
    }

    @PostMapping("/accounts/remit")
    public ResponseEntity<AccountDTO.RemitResponse> remit(
        @RequestBody @Valid AccountDTO.RemitRequest remitRequest,
        @SigninMember Member remitter) {

        Transaction transaction = accountService.remit(remitRequest.toTransaction(), remitter);

        return ResponseEntity.status(HttpStatus.OK)
                .body(AccountDTO.RemitResponse.from(transaction));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDTO.GetAccountResponse> getAccountInfo(
            @PathVariable String accountNumber,
            @SigninMember Member member
    ) {

        Account.validateAccountNumber(accountNumber);
        Account account = accountService.getAccountInfo(accountNumber, member);

        return ResponseEntity.status(HttpStatus.OK)
                .body(AccountDTO.GetAccountResponse.from(account));
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<AccountDTO.GetTransactionResponse>> getTransactions(
            @PathVariable String accountNumber,
            @SigninMember Member member,
            Pageable pageable) {

        Account.validateAccountNumber(accountNumber);
        List<Transaction> transactions = accountService.getTransactions(accountNumber, member, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(transactions.stream()
                        .map(AccountDTO.GetTransactionResponse::from)
                        .toList());
    }
}
