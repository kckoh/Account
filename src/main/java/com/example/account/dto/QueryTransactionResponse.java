package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;



@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class QueryTransactionResponse {

    private String accountNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResultType;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactedAt;



    public static QueryTransactionResponse from(TransactionDto transactionDto)  {
        return QueryTransactionResponse.builder()
                .accountNumber(transactionDto.getAccoutNumber())
                .transactionType(transactionDto.getTransactionType())
                .transactionResultType(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTranscationId())
                .transactedAt(transactionDto.getTransactedAt())
                .amount(transactionDto.getAmount())
                .build();

    }
}
