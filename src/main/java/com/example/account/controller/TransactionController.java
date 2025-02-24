package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(@PathVariable String transactionId) {
        return QueryTransactionResponse.from(transactionService.queryTransaction(transactionId));
    }
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(@Valid @RequestBody UseBalance.Request request) {

        try {
            return UseBalance.Response.from(transactionService.useBalance(request.getUserId(), request.getAccountNumber(), request.getAmount()));

        } catch (AccountException e) {
            log.error("Failed to use balance.");
            transactionService.saveFailedUseTransaction(request.getAccountNumber(), request.getAmount());
            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(@Valid @RequestBody CancelBalance.Request request) {

        try {
            return CancelBalance.Response.from(transactionService.cancelBalance(request.getTransactionId(), request.getAccountNumber(), request.getAmount()));

        } catch (AccountException e) {
            log.error("Failed to use balance.");
            transactionService.saveFailedCancelTransaction(request.getAccountNumber(), request.getAmount());
            throw e;
        }
    }
}
