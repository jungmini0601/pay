package com.jungmini.pay.controller;

import com.jungmini.pay.common.resolover.SigninMember;
import com.jungmini.pay.controller.dto.AccountDTO;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
