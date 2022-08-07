package com.example.account.dto;

import com.example.account.domain.Account;
import com.example.account.domain.Transaction;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.USE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {

    private String accoutNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResultType;
    private Long amount;
    private Long balanceSnapshot;
    private String transcationId;
    private LocalDateTime transactedAt;


    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .accoutNumber(transaction.getAccount().getAccountNumber())
                .transactionType(transaction.getTransactionType())
                .transactionResultType(transaction.getTransactionResultType())
                .amount(transaction.getAmount())
                .balanceSnapshot(transaction.getBalanceSnapshot())
                .transcationId(transaction.getTranscationId())
                .transactedAt(transaction.getTransactedAt())
                .build();
    }
}

