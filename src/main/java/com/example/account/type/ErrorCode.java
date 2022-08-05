package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("NO USER IS FOUND"), MAX_ACCOUNT_PER_USER_10("THE MAX USER ACCOUNT IS 10");
    private final String description;
}
