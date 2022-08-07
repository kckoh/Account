package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("NO USER IS FOUND"),
    MAX_ACCOUNT_PER_USER_10("THE MAX USER ACCOUNT IS 10"),
    ACCOUNT_NOT_FOUND("ACCOUNT NUMBER IS NOT FOUND"),
    USER_ACCOUNT_UNMATCH("USER ACCOUNT IS NOT MATCHED"),
    ACCOUNT_ALREDAY_REGISTERED("ACCOUNT ALREADY UNGISTERED"),
    ACCOUNT_ALREDAY_UNREGISTERED("ACCOUNT ALREADY UNREGISTERED"),

    BALANCE_NOT_EMPTY("ACCOUNT BALANCE SHOULD NOT BE EMPTY"), AMOUNT_EXCEED_BALANCE("TRANSACTION AMOUNT IS GREATER THAN THE BALANCE");
    private final String description;
}
