package com.example.account.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class AccountUser {
    @Id
    @Generated
    private long id;

    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
