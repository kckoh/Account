package com.example.account.controller;

import com.example.account.dto.UseBalance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(@Valid @RequestBody UseBalance.Request request) {

        return new UseBalance.Response();
    }
}
