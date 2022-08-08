package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance(){
        //given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .transactionType(USE)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now())
                                .balanceSnapshot(9000L)
                                .amount(1000L)
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 2000L);

        //then
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
//        assertEquals("1000000012", transactionDto.getAccoutNumber());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());

        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(2000L, captor.getValue().getAmount());
        assertEquals(8000L, captor.getValue().getBalanceSnapshot());

    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void useBalance_UserNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());




//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1234567890", 1000L));
//        then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void useBalance_AccountNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());





//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1234567890", 1000L));
//        then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());

    }


    @Test
    @DisplayName("unmatched user")
    void useBalance_userUnMatch() {
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
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1234567890",1000L));
//        then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCH, accountException.getErrorCode());

    }


    @Test
    @DisplayName("Balance Not empty")
    void useBalance_alreadyUnregistered() {
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
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1234567890",1000L));
//        then
        assertEquals(ErrorCode.ACCOUNT_ALREDAY_UNREGISTERED, accountException.getErrorCode());

    }

    @Test
    void useBalance_exceedBalance(){
        //given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));



        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1234567890",20000L));


        //then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, accountException.getErrorCode());


    }

    @Test
    void saveFailedUseTransaction(){
        //given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();


        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .transactionType(USE)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now())
                                .balanceSnapshot(9000L)
                                .amount(1000L)
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        transactionService.saveFailedUseTransaction( "1000000000", 2000L);

        //then

        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(2000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());

    }


    @Test
    void successCancelBalance(){
        //given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transcationId("id")
                .transactedAt(LocalDateTime.now())
                .balanceSnapshot(10000L)
                .amount(1000L)
                .build();
        given(transactionRepository.findByTranscationId(anyString()))
                .willReturn(Optional.of(transaction));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now())
                                .balanceSnapshot(10000L)
                                .amount(1000L)
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.cancelBalance("id", "1000000000", 1000L);

        //then
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());

        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(11000L, captor.getValue().getBalanceSnapshot());

    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void cancelBalance_AccountNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(transactionRepository.findByTranscationId(any()))
                .willReturn(Optional.of(
                        Transaction.builder()
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now())
                                .balanceSnapshot(10000L)
                                .amount(1000L)
                                .build()
                ));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());





//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.cancelBalance("id", "1234567890", 1000L));
//        then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("원 사용 거래 없음 - 잔액 사용 취소 실패")
    void cancelBalance_TransactionNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(transactionRepository.findByTranscationId(any()))
                .willReturn(Optional.empty());



//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.cancelBalance("id", "1234567890", 1000L));
//        then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("거래와 계좌가 매칭실패 - 계좌 해지 실패")
    void cancelBalance_transactionAccountUnMatch() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();

        Account accountNotUse = Account.builder()
                .accountUser(user)
                .id(2L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000013")
                .balance(10000L)
                .build();


        given(transactionRepository.findByTranscationId(any()))
                .willReturn(Optional.of(
                        Transaction.builder()
                                .account(account)
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now())
                                .balanceSnapshot(10000L)
                                .amount(1000L)
                                .build()
                ));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUse));



//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.cancelBalance("id", "1234567890", 1000L));
//        then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, accountException.getErrorCode());

    }


    @Test
    @DisplayName("거래와 취소 금액이 다름 - 작액 사용 취소 실패")
    void cancelBalance_cancelMustFully() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();



        given(transactionRepository.findByTranscationId(any()))
                .willReturn(Optional.of(
                        Transaction.builder()
                                .account(account)
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now())
                                .balanceSnapshot(10000L)
                                .amount(2000L)
                                .build()
                ));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));



//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.cancelBalance("id", "1234567890", 1000L));
//        then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, accountException.getErrorCode());

    }


    @Test
    @DisplayName("거래와 취소 금액이 다름 - 작액 사용 취소 실패")
    void cancelBalance_tooOldToCancel() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();



        given(transactionRepository.findByTranscationId(any()))
                .willReturn(Optional.of(
                        Transaction.builder()
                                .account(account)
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now().minusYears(2))
                                .balanceSnapshot(10000L)
                                .amount(2000L)
                                .build()
                ));


        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));



//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.cancelBalance("id", "1234567890", 2000L));
//        then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, accountException.getErrorCode());

    }


    @Test
    void successQueryTransaction(){
        //given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
//                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(transactionRepository.findByTranscationId(anyString()))
                .willReturn(Optional.of(
                        Transaction.builder()
                                .account(account)
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .transcationId("id")
                                .transactedAt(LocalDateTime.now().minusYears(2))
                                .balanceSnapshot(10000L)
                                .amount(2000L)
                                .build()
                ));
        //when
        TransactionDto transactionDto = transactionService.queryTransaction(anyString());
        //then
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals("id", transactionDto.getTranscationId());
        assertEquals(2000L, transactionDto.getAmount());
    }

    @Test
    @DisplayName("원 거래 없음 - 잔액 조회 실패")
    void queryTranslation_TransactionNotFound() {
//        given
        AccountUser user = AccountUser.builder().id(12L).name("hello").build();
        given(transactionRepository.findByTranscationId(any()))
                .willReturn(Optional.empty());



//        when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.queryTransaction("id"));
//        then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());

    }




}