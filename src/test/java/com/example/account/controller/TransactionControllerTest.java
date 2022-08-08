package com.example.account.controller;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CancelBalance;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.example.account.type.TransactionType.USE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successUseBalance() throws Exception {
        //given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto
                        .builder()
                        .accoutNumber("1234567890")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .transcationId("id")
                        .transactionResultType(TransactionResultType.S)
                        .build());

        //when
        mockMvc.perform(post("/transaction/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UseBalance.Request(1L, "2000000000", 3000L))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionId").value("id"))
                .andExpect(jsonPath("$.amount").value(1000L));



        //then
    }

    @Test
    void successCancelBalance() throws Exception {
        //given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto
                        .builder()
                        .accoutNumber("1234567890")
                        .transactedAt(LocalDateTime.now())
                        .amount(3000L)
                        .transcationId("id")
                        .transactionResultType(TransactionResultType.S)
                        .build());

        //when
        mockMvc.perform(post("/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelBalance.Request("some id", "2000000000", 3000L))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionId").value("id"))
                .andExpect(jsonPath("$.amount").value(3000L));



        //then
    }


    @Test
    void successGetAccountsByUserId() throws Exception {
        //given

        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto
                        .builder()
                        .transactionType(USE)
                        .accoutNumber("1234567890")
                        .transactedAt(LocalDateTime.now())
                        .amount(3000L)
                        .transcationId("id")
                        .transactionResultType(TransactionResultType.S)
                        .build());
        //when

        //then
        mockMvc.perform(get("/transaction/id"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionId").value("id"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.transactionResultType").value("S"))
                .andExpect(jsonPath("$.amount").value(3000L));

    }

}