package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012")
                        .build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//        when
        AccountDto accountDto = accountService.createAccount(1L, 10000L);
//        then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013",captor.getValue().getAccountNumber());

    }

    @Test
    void createFirstAccount() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//        when
        AccountDto accountDto = accountService.createAccount(1L, 10000L);
//        then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000000",captor.getValue().getAccountNumber());

    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());




//        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));
//        then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    void createAccount_maxAccountIs10() {

        //given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any())).willReturn(10);

        //        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));
//        then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());
    }



    @Test
    void deleteAccountSuccess() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));

//        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//        when
        AccountDto accountDto = accountService.deleteAccount(1L, "0123456789");
//        then
//        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
//        assertEquals("1000000012",captor.getValue().getAccountNumber());

    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());




//        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1234567890"));
//        then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());





//        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1234567890"));
//        then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("unmatched user")
    void deleteAccountFailed_userUnMatch() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        AccountUser otherUser = AccountUser.builder().id(13L).name("hello2 ").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));

        //        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1234567890"));
//        then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCH, accountException.getErrorCode());

    }


    @Test
    @DisplayName("Balance Not empty")
    void deleteAccountFailed_balanceNotEmpty() {
//        given
        AccountUser otherUser = AccountUser.builder().id(13L).name("hello2 ").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(otherUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .accountNumber("1000000012")
                        .balance(10L)
                        .build()));

        //        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1234567890"));
//        then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());

    }

    @Test
    @DisplayName("Balance Not empty")
    void deleteAccountFailed_alreadyUnregistered() {
//        given
        AccountUser otherUser = AccountUser.builder().id(13L).name("hello2 ").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(otherUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("1000000012")
                        .balance(10L)
                        .build()));

        //        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1234567890"));
//        then
        assertEquals(ErrorCode.ACCOUNT_ALREDAY_UNREGISTERED, accountException.getErrorCode());

    }

    @Test
    void successGetAccountByUserid(){
        //given
        AccountUser ac = AccountUser.builder()
                .id(1L)
                .name("hello")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        List<Account> accounts = Arrays.asList(
                Account.builder().accountUser(ac).accountNumber("1234567890").balance(1000L).build(),
                Account.builder().accountUser(ac).accountNumber("1234567891").balance(2000L).build()
        );

        given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(ac));
        given(accountRepository.findByAccountUser(any())).willReturn(accounts);


        //when
        List<AccountDto> accountDtos = accountService.getAccoutsByUserId(1L);

        //then
        assertEquals(2, accountDtos.size());
        assertEquals("1234567890", accountDtos.get(0).getAccountNumber());
        assertEquals(1000L, accountDtos.get(0).getBalance());

        assertEquals("1234567891", accountDtos.get(1).getAccountNumber());
        assertEquals(2000L, accountDtos.get(1).getBalance());

    }
    @Test
    void failGetAccountByUserid(){
        //given
        given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty());



        //when


        //        when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.getAccoutsByUserId(1L));
//        then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }

//    @Test
//    @DisplayName("계좌 조회 성공")
//    void testXXX() {
//        //given
//        given(accountRepository.findById(anyLong()))
//                .willReturn(Optional.of(Account.builder()
//                        .accountStatus(AccountStatus.UNREGISTERED)
//                        .accountNumber("65789").build()));
//        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
//
//        //when
//        Account account = accountService.getAccount(4555L);
//
//        //then
//        verify(accountRepository, times(1)).findById(captor.capture());
//        verify(accountRepository, times(0)).save(any());
//        assertEquals(4555L, captor.getValue());
//        assertNotEquals(45515L, captor.getValue());
//        assertEquals("65789", account.getAccountNumber());
//        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
//    }
//
//    @Test
//    @DisplayName("계좌 조회 실패 - 음수로 조회")
//    void testFailedToSearchAccount() {
//        //given
//        //when
//        RuntimeException exception = assertThrows(RuntimeException.class,
//                () -> accountService.getAccount(-10L));
//
//        //then
//        assertEquals("Minus", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Test 이름 변경")
//    void testGetAccount() {
//        //given
//        given(accountRepository.findById(anyLong()))
//                .willReturn(Optional.of(Account.builder()
//                        .accountStatus(AccountStatus.UNREGISTERED)
//                        .accountNumber("65789").build()));
//
//        //when
//        Account account = accountService.getAccount(4555L);
//
//        //then
//        assertEquals("65789", account.getAccountNumber());
//        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
//    }
//
//    @Test
//    void testGetAccount2() {
//        //given
//        given(accountRepository.findById(anyLong()))
//                .willReturn(Optional.of(Account.builder()
//                        .accountStatus(AccountStatus.UNREGISTERED)
//                        .accountNumber("65789").build()));
//
//        //when
//        Account account = accountService.getAccount(4555L);
//
//        //then
//        assertEquals("65789", account.getAccountNumber());
//        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
//    }


}