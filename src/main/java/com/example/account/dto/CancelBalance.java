package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

public class CancelBalance {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotBlank
        private String transactionId;

        @NotNull
        @Size(min = 10, max = 10)
        private String accountNumber;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private String accountNumber;
        private TransactionResultType transactionResultType;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;



        public static Response from(TransactionDto transactionDto)  {
                return Response.builder()
                        .accountNumber(transactionDto.getAccoutNumber())
                        .transactionResultType(transactionDto.getTransactionResultType())
                        .transactionId(transactionDto.getTranscationId())
                        .transactedAt(transactionDto.getTransactedAt())
                        .amount(transactionDto.getAmount())
                        .build();

        }
    }
}
