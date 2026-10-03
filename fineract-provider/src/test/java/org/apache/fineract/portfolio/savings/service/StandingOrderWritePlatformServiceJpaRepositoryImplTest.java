/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.data.StandingOrderDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.StandingOrder;
import org.apache.fineract.portfolio.savings.domain.StandingOrderFrequency;
import org.apache.fineract.portfolio.savings.domain.StandingOrderRepository;
import org.apache.fineract.portfolio.savings.domain.StandingOrderStatus;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StandingOrderWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private StandingOrderRepository standingOrderRepository;
    @Mock
    private StandingOrderDataValidator standingOrderDataValidator;
    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;
    @Mock
    private SavingsAccountTransactionRepository savingsAccountTransactionRepository;

    private final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();

    private StandingOrderWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new StandingOrderWritePlatformServiceJpaRepositoryImpl(
                context, fromApiJsonHelper, standingOrderRepository, standingOrderDataValidator,
                savingsAccountRepositoryWrapper, savingsAccountDomainService, savingsAccountTransactionRepository);
    }

    @Test
    void testCreateStandingOrder_Success() {
        AppUser currentUser = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(currentUser);

        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.getId()).thenReturn(1L);
        when(sourceAccount.clientId()).thenReturn(10L);
        when(sourceAccount.officeId()).thenReturn(1L);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(sourceAccount);

        SavingsAccount destAccount = mock(SavingsAccount.class);
        when(destAccount.getId()).thenReturn(2L);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(destAccount);

        when(standingOrderRepository.save(any(StandingOrder.class))).thenAnswer(invocation -> {
            StandingOrder so = invocation.getArgument(0);
            so.setId(55L);
            return so;
        });

        String json = "{\n"
                + "  \"name\": \"Monthly Transfer\",\n"
                + "  \"sourceAccountId\": 1,\n"
                + "  \"destinationAccountId\": 2,\n"
                + "  \"amount\": 150000,\n"
                + "  \"frequency\": \"MONTHLY\",\n"
                + "  \"startDate\": \"2026-10-05\",\n"
                + "  \"endDate\": \"2027-10-05\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn(json);

        CommandProcessingResult result = service.createStandingOrder(command);

        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isEqualTo(55L);
        assertThat(result.getClientId()).isEqualTo(10L);
        verify(standingOrderDataValidator).validateForCreate(json);
    }

    @Test
    void testApproveStandingOrder_SelfApprovalBlocked_ThrowsException() {
        AppUser creator = mock(AppUser.class);
        when(creator.getId()).thenReturn(5L);

        StandingOrder order = new StandingOrder();
        order.setId(10L);
        order.setStatus(StandingOrderStatus.PENDING);
        order.setCreatedBy(creator);

        when(standingOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(5L);
        when(context.authenticatedUser()).thenReturn(approver);

        JsonCommand command = mock(JsonCommand.class);

        assertThatThrownBy(() -> service.approveStandingOrder(10L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Maker of the standing order cannot approve it");
    }

    @Test
    void testApproveStandingOrder_DifferentUser_SetsRunning() {
        AppUser creator = mock(AppUser.class);
        when(creator.getId()).thenReturn(5L);

        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.clientId()).thenReturn(25L);

        StandingOrder order = new StandingOrder();
        order.setId(10L);
        order.setStatus(StandingOrderStatus.PENDING);
        order.setCreatedBy(creator);
        order.setSourceAccount(sourceAccount);

        when(standingOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(9L);
        when(context.authenticatedUser()).thenReturn(approver);

        JsonCommand command = mock(JsonCommand.class);

        CommandProcessingResult result = service.approveStandingOrder(10L, command);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(StandingOrderStatus.RUNNING);
        assertThat(order.getApprovedBy()).isEqualTo(approver);
        assertThat(order.getApprovedAt()).isNotNull();
    }

    @Test
    void testRejectStandingOrder_PendingOrder_SetsRejected() {
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.clientId()).thenReturn(25L);

        StandingOrder order = new StandingOrder();
        order.setId(12L);
        order.setStatus(StandingOrderStatus.PENDING);
        order.setSourceAccount(sourceAccount);

        when(standingOrderRepository.findById(12L)).thenReturn(Optional.of(order));

        AppUser rejecter = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(rejecter);

        JsonCommand command = mock(JsonCommand.class);
        when(command.stringValueOfParameterNamed("rejectionReason")).thenReturn("Duplicate request");

        CommandProcessingResult result = service.rejectStandingOrder(12L, command);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(StandingOrderStatus.REJECTED);
        assertThat(order.getRejectionReason()).isEqualTo("Duplicate request");
    }

    @Test
    void testExecuteStandingOrder_SufficientBalance_DebitsSourceCreditsDestination() {
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.clientId()).thenReturn(25L);

        SavingsAccount destAccount = mock(SavingsAccount.class);

        StandingOrder order = new StandingOrder();
        order.setId(15L);
        order.setStatus(StandingOrderStatus.RUNNING);
        order.setAmount(new BigDecimal("100000"));
        order.setSourceAccount(sourceAccount);
        order.setDestinationAccount(destAccount);
        order.setEndDate(LocalDate.now().plusMonths(6));

        when(standingOrderRepository.findById(15L)).thenReturn(Optional.of(order));
        when(standingOrderDataValidator.calculateAvailableBalance(sourceAccount)).thenReturn(new BigDecimal("300000"));

        SavingsAccountTransaction withdrawalTxn = mock(SavingsAccountTransaction.class);
        when(savingsAccountDomainService.handleWithdrawal(eq(sourceAccount), any(), any(), eq(new BigDecimal("100000")),
                any(), any(SavingsTransactionBooleanValues.class), anyBoolean())).thenReturn(withdrawalTxn);

        SavingsAccountTransaction depositTxn = mock(SavingsAccountTransaction.class);
        when(savingsAccountDomainService.handleDeposit(eq(destAccount), any(), any(), eq(new BigDecimal("100000")),
                any(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(depositTxn);

        CommandProcessingResult result = service.executeStandingOrder(15L);

        assertThat(result).isNotNull();
        assertThat(order.getLastRunDate()).isEqualTo(LocalDate.now());
        assertThat(order.getStatus()).isEqualTo(StandingOrderStatus.RUNNING);
        verify(savingsAccountDomainService).handleWithdrawal(eq(sourceAccount), any(), any(), eq(new BigDecimal("100000")),
                any(), any(SavingsTransactionBooleanValues.class), anyBoolean());
        verify(savingsAccountDomainService).handleDeposit(eq(destAccount), any(), any(), eq(new BigDecimal("100000")),
                any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void testExecuteStandingOrder_InsufficientBalance_ThrowsException() {
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        SavingsAccount destAccount = mock(SavingsAccount.class);

        StandingOrder order = new StandingOrder();
        order.setId(16L);
        order.setStatus(StandingOrderStatus.RUNNING);
        order.setAmount(new BigDecimal("200000"));
        order.setSourceAccount(sourceAccount);
        order.setDestinationAccount(destAccount);

        when(standingOrderRepository.findById(16L)).thenReturn(Optional.of(order));
        when(standingOrderDataValidator.calculateAvailableBalance(sourceAccount)).thenReturn(new BigDecimal("50000"));

        assertThatThrownBy(() -> service.executeStandingOrder(16L))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Insufficient available balance in source savings account");
    }
}
