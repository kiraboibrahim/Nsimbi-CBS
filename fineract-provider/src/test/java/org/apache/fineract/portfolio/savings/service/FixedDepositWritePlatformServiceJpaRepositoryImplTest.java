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
import static org.mockito.Mockito.never;
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
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.data.FixedDepositDataValidator;
import org.apache.fineract.portfolio.savings.domain.FixedDeposit;
import org.apache.fineract.portfolio.savings.domain.FixedDepositFundingSource;
import org.apache.fineract.portfolio.savings.domain.FixedDepositInterestInterval;
import org.apache.fineract.portfolio.savings.domain.FixedDepositPayoutOption;
import org.apache.fineract.portfolio.savings.domain.FixedDepositRepository;
import org.apache.fineract.portfolio.savings.domain.FixedDepositStatus;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
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
class FixedDepositWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private FixedDepositDataValidator validator;
    @Mock
    private FixedDepositRepository fixedDepositRepository;
    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    @Mock
    private SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;

    private final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();

    private FixedDepositWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new FixedDepositWritePlatformServiceJpaRepositoryImpl(
                context, fromApiJsonHelper, fixedDepositRepository, validator,
                clientRepository, savingsAccountRepositoryWrapper,
                savingsAccountDomainService, savingsAccountTransactionRepository);
    }

    @Test
    void testCreateFixedDeposit_TillCash_PendingStatus() {
        AppUser currentUser = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(currentUser);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);
        when(client.getDisplayName()).thenReturn("John Doe");
        when(client.mobileNo()).thenReturn("+256700000000");
        when(clientRepository.findOneWithNotFoundDetection(1L)).thenReturn(client);

        SavingsAccount payoutAccount = mock(SavingsAccount.class);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(payoutAccount);

        when(fixedDepositRepository.save(any(FixedDeposit.class))).thenAnswer(invocation -> {
            FixedDeposit fd = invocation.getArgument(0);
            fd.setId(100L);
            return fd;
        });

        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"principalAmount\": 1000000,\n"
                + "  \"periodMonths\": 12,\n"
                + "  \"interestRateAnnual\": 12.0,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"TILL_CASH\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn(json);

        CommandProcessingResult result = service.createFixedDeposit(command);

        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isEqualTo(100L);
        assertThat(result.getClientId()).isEqualTo(1L);
        verify(validator).validateForCreate(json);
    }

    @Test
    void testCreateFixedDeposit_SavingsOffset_PlacesHold() {
        AppUser currentUser = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(currentUser);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);
        when(client.getDisplayName()).thenReturn("Jane Doe");
        when(clientRepository.findOneWithNotFoundDetection(1L)).thenReturn(client);

        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(5L)).thenReturn(sourceAccount);

        when(fixedDepositRepository.save(any(FixedDeposit.class))).thenAnswer(invocation -> {
            FixedDeposit fd = invocation.getArgument(0);
            fd.setId(101L);
            return fd;
        });

        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"fundingSavingsAccountId\": 5,\n"
                + "  \"principalAmount\": 500000,\n"
                + "  \"periodMonths\": 6,\n"
                + "  \"interestRateAnnual\": 10.0,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 5,\n"
                + "  \"fundingSource\": \"SAVINGS_OFFSET\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn(json);

        CommandProcessingResult result = service.createFixedDeposit(command);

        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isEqualTo(101L);
        verify(sourceAccount).holdAmount(new BigDecimal("500000"));
        verify(savingsAccountRepositoryWrapper).save(sourceAccount);
    }

    @Test
    void testApproveFixedDeposit_SelfApprovalBlocked_ThrowsException() {
        AppUser creator = mock(AppUser.class);
        when(creator.getId()).thenReturn(10L);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(50L);
        deposit.setStatus(FixedDepositStatus.PENDING);
        deposit.setCreatedBy(creator);
        deposit.setCustomer(client);

        when(fixedDepositRepository.findById(50L)).thenReturn(Optional.of(deposit));

        // Approver is same user (ID 10)
        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(10L);
        when(context.authenticatedUser()).thenReturn(approver);

        JsonCommand command = mock(JsonCommand.class);

        assertThatThrownBy(() -> service.approveFixedDeposit(50L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Maker of the fixed deposit placement cannot approve it");
    }

    @Test
    void testApproveFixedDeposit_DifferentUser_SavingsOffset_WithdrawsAndSetsRunning() {
        AppUser creator = mock(AppUser.class);
        when(creator.getId()).thenReturn(10L);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(5L)).thenReturn(sourceAccount);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(50L);
        deposit.setStatus(FixedDepositStatus.PENDING);
        deposit.setFundingSource(FixedDepositFundingSource.SAVINGS_OFFSET);
        deposit.setFundingSavingsAccount(sourceAccount);
        deposit.setPrincipalAmount(new BigDecimal("500000"));
        deposit.setCreatedBy(creator);
        deposit.setCustomer(client);

        when(fixedDepositRepository.findById(50L)).thenReturn(Optional.of(deposit));

        // Approver is different user (ID 20)
        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(20L);
        when(context.authenticatedUser()).thenReturn(approver);

        SavingsAccountTransaction withdrawalTxn = mock(SavingsAccountTransaction.class);
        when(savingsAccountDomainService.handleWithdrawal(eq(sourceAccount), any(), any(), eq(new BigDecimal("500000")),
                any(), any(SavingsTransactionBooleanValues.class), anyBoolean())).thenReturn(withdrawalTxn);

        when(fixedDepositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JsonCommand command = mock(JsonCommand.class);

        CommandProcessingResult result = service.approveFixedDeposit(50L, command);

        assertThat(result).isNotNull();
        assertThat(deposit.getStatus()).isEqualTo(FixedDepositStatus.RUNNING);
        assertThat(deposit.getApprovedBy()).isEqualTo(approver);
        assertThat(deposit.getApprovedAt()).isNotNull();
        verify(sourceAccount).releaseOnHoldAmount(new BigDecimal("500000"));
        verify(savingsAccountDomainService).handleWithdrawal(eq(sourceAccount), any(), any(), eq(new BigDecimal("500000")),
                any(), any(SavingsTransactionBooleanValues.class), anyBoolean());
    }

    @Test
    void testApproveFixedDeposit_DifferentUser_TillCash_SetsRunningWithoutWithdrawal() {
        AppUser creator = mock(AppUser.class);
        when(creator.getId()).thenReturn(10L);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(51L);
        deposit.setStatus(FixedDepositStatus.PENDING);
        deposit.setFundingSource(FixedDepositFundingSource.TILL_CASH);
        deposit.setPrincipalAmount(new BigDecimal("1000000"));
        deposit.setCreatedBy(creator);
        deposit.setCustomer(client);

        when(fixedDepositRepository.findById(51L)).thenReturn(Optional.of(deposit));

        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(20L);
        when(context.authenticatedUser()).thenReturn(approver);
        when(fixedDepositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JsonCommand command = mock(JsonCommand.class);

        CommandProcessingResult result = service.approveFixedDeposit(51L, command);

        assertThat(result).isNotNull();
        assertThat(deposit.getStatus()).isEqualTo(FixedDepositStatus.RUNNING);
        verify(savingsAccountDomainService, never()).handleWithdrawal(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void testApproveFixedDeposit_NotPending_ThrowsException() {
        AppUser creator = mock(AppUser.class);
        when(creator.getId()).thenReturn(10L);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(52L);
        deposit.setStatus(FixedDepositStatus.RUNNING);
        deposit.setCreatedBy(creator);

        when(fixedDepositRepository.findById(52L)).thenReturn(Optional.of(deposit));

        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(20L);
        when(context.authenticatedUser()).thenReturn(approver);

        JsonCommand command = mock(JsonCommand.class);

        assertThatThrownBy(() -> service.approveFixedDeposit(52L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Fixed deposit cannot be approved in current status");
    }

    @Test
    void testRejectFixedDeposit_SavingsOffset_ReleasesHoldAndSetsRejected() {
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(5L)).thenReturn(sourceAccount);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(61L);
        deposit.setStatus(FixedDepositStatus.PENDING);
        deposit.setFundingSource(FixedDepositFundingSource.SAVINGS_OFFSET);
        deposit.setFundingSavingsAccount(sourceAccount);
        deposit.setPrincipalAmount(new BigDecimal("500000"));
        deposit.setCustomer(client);

        when(fixedDepositRepository.findById(61L)).thenReturn(Optional.of(deposit));

        AppUser rejecter = mock(AppUser.class);
        when(rejecter.getId()).thenReturn(20L);
        when(context.authenticatedUser()).thenReturn(rejecter);
        when(fixedDepositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JsonCommand command = mock(JsonCommand.class);
        when(command.stringValueOfParameterNamed("rejectionReason")).thenReturn("Member requested cancellation");

        CommandProcessingResult result = service.rejectFixedDeposit(61L, command);

        assertThat(result).isNotNull();
        assertThat(deposit.getStatus()).isEqualTo(FixedDepositStatus.REJECTED);
        assertThat(deposit.getRejectionReason()).isEqualTo("Member requested cancellation");
        verify(sourceAccount).releaseOnHoldAmount(new BigDecimal("500000"));
        verify(savingsAccountRepositoryWrapper).save(sourceAccount);
    }

    @Test
    void testTerminateFixedDeposit_NotRunning_ThrowsException() {
        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(70L);
        deposit.setStatus(FixedDepositStatus.PENDING);

        when(fixedDepositRepository.findById(70L)).thenReturn(Optional.of(deposit));

        JsonCommand command = mock(JsonCommand.class);

        assertThatThrownBy(() -> service.terminateFixedDeposit(70L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Fixed deposit cannot be terminated in status");
    }

    @Test
    void testTerminateFixedDeposit_WithInterest_CalculatesAccruedInterestAndCreditsPayoutAccount() {
        AppUser currentUser = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(currentUser);

        SavingsAccount payoutAccount = mock(SavingsAccount.class);
        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(71L);
        deposit.setStatus(FixedDepositStatus.RUNNING);
        deposit.setPrincipalAmount(new BigDecimal("1000000"));
        deposit.setInterestRateAnnual(new BigDecimal("12.0"));
        deposit.setStartsOn(LocalDate.now().minusDays(100));
        deposit.setPayoutOption(FixedDepositPayoutOption.PRINCIPAL_AND_INTEREST);
        deposit.setPayoutSavingsAccount(payoutAccount);
        deposit.setCustomer(client);

        when(fixedDepositRepository.findById(71L)).thenReturn(Optional.of(deposit));
        when(fixedDepositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SavingsAccountTransaction depositTxn = mock(SavingsAccountTransaction.class);
        when(savingsAccountDomainService.handleDeposit(eq(payoutAccount), any(), any(), any(BigDecimal.class),
                any(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(depositTxn);

        JsonCommand command = mock(JsonCommand.class);
        when(command.stringValueOfParameterNamed("interestDecision")).thenReturn("WITH_INTEREST");
        when(command.localDateValueOfParameterNamed("terminationDate")).thenReturn(LocalDate.now());

        CommandProcessingResult result = service.terminateFixedDeposit(71L, command);

        assertThat(result).isNotNull();
        assertThat(deposit.getStatus()).isEqualTo(FixedDepositStatus.TERMINATED_WITH_INTEREST);
        assertThat(deposit.getAccruedInterest()).isNotNull();
        assertThat(deposit.getAccruedInterest()).isGreaterThan(BigDecimal.ZERO);

        // Payout amount should be principal + accrued interest
        BigDecimal expectedPayout = deposit.getPrincipalAmount().add(deposit.getAccruedInterest());
        verify(savingsAccountDomainService).handleDeposit(eq(payoutAccount), any(), any(), eq(expectedPayout),
                any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void testTerminateFixedDeposit_NoInterest_CreditsPrincipalOnly() {
        AppUser currentUser = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(currentUser);

        SavingsAccount payoutAccount = mock(SavingsAccount.class);
        Client client = mock(Client.class);
        when(client.getId()).thenReturn(1L);

        FixedDeposit deposit = new FixedDeposit();
        deposit.setId(72L);
        deposit.setStatus(FixedDepositStatus.RUNNING);
        deposit.setPrincipalAmount(new BigDecimal("1000000"));
        deposit.setInterestRateAnnual(new BigDecimal("12.0"));
        deposit.setStartsOn(LocalDate.now().minusDays(100));
        deposit.setPayoutOption(FixedDepositPayoutOption.PRINCIPAL_AND_INTEREST);
        deposit.setPayoutSavingsAccount(payoutAccount);
        deposit.setCustomer(client);

        when(fixedDepositRepository.findById(72L)).thenReturn(Optional.of(deposit));
        when(fixedDepositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SavingsAccountTransaction depositTxn = mock(SavingsAccountTransaction.class);
        when(savingsAccountDomainService.handleDeposit(eq(payoutAccount), any(), any(), any(BigDecimal.class),
                any(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(depositTxn);

        JsonCommand command = mock(JsonCommand.class);
        when(command.stringValueOfParameterNamed("interestDecision")).thenReturn("NO_INTEREST");
        when(command.localDateValueOfParameterNamed("terminationDate")).thenReturn(LocalDate.now());

        CommandProcessingResult result = service.terminateFixedDeposit(72L, command);

        assertThat(result).isNotNull();
        assertThat(deposit.getStatus()).isEqualTo(FixedDepositStatus.TERMINATED_NO_INTEREST);
        assertThat(deposit.getAccruedInterest()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(savingsAccountDomainService).handleDeposit(eq(payoutAccount), any(), any(), eq(new BigDecimal("1000000")),
                any(), anyBoolean(), anyBoolean(), anyBoolean());
    }
}
