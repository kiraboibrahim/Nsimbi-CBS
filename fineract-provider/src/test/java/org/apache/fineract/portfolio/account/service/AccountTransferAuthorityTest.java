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
package org.apache.fineract.portfolio.account.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.domain.SavingsTransactionOrigin;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthority;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthorityRepository;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDataValidator;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.handler.CreateAccountTransferCommandHandler;
import org.apache.fineract.portfolio.account.handler.RefundByTransferCommandHandler;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.test.util.ReflectionTestUtils;

class AccountTransferAuthorityTest {

    private final FromJsonHelper json = new FromJsonHelper();
    private final NsimbiUserMonetaryAuthorityRepository limits = mock(NsimbiUserMonetaryAuthorityRepository.class);
    private final SavingsAccountRepositoryWrapper accounts = mock(SavingsAccountRepositoryWrapper.class);
    private final AccountTransfersWritePlatformService writes = mock(AccountTransfersWritePlatformService.class);
    private final AppUser maker = mock(AppUser.class);
    private final AppUser checker = mock(AppUser.class);
    private CreateAccountTransferCommandHandler handler;
    private AccountTransferAuthorityService authority;
    private final LoanRepositoryWrapper loans = mock(LoanRepositoryWrapper.class);

    @BeforeEach
    void setup() {
        when(maker.getId()).thenReturn(7L);
        when(checker.getId()).thenReturn(8L);
        SavingsAccount source = mock(SavingsAccount.class);
        when(source.getCurrency()).thenReturn(new MonetaryCurrency("UGX", 2, 0));
        when(accounts.findOneWithNotFoundDetection(42L)).thenReturn(source);
        authority = new AccountTransferAuthorityService(
                new NsimbiMonetaryAuthorityPolicyService(limits), accounts, loans,
                mock(AccountTransfersDataValidator.class), mock(StandingInstructionDataValidator.class),
                mock(StandingInstructionRepository.class));
        handler = new CreateAccountTransferCommandHandler(authority, writes);
        when(writes.create(any())).thenReturn(CommandProcessingResult.empty());
        configure("10", "100");
    }

    @ParameterizedTest
    @ValueSource(strings = { "10", "50", "100" })
    void inclusivePrincipalBoundsUseSourceCurrency(String amount) {
        JsonCommand command = command(payload(amount));
        handler.processTransaction(command, context());
        verify(writes).create(command);
        verify(limits).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX");
        verifyNoMoreInteractions(limits);
    }

    @ParameterizedTest
    @ValueSource(strings = { "9.99", "100.01" })
    void deniedPrincipalNeverCallsFinancialWriter(String amount) {
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processTransaction(command(payload(amount)), context()));
        verifyNoInteractions(writes);
    }

    @Test
    void missingAndUnconfiguredLimitsFailClosed() {
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX"))
                .thenReturn(Optional.empty());
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processTransaction(command(payload("50")), context()));
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(7L, MonetaryAuthorityType.TRANSFER, "UGX", null, null)));
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processTransaction(command(payload("50")), context()));
        verifyNoInteractions(writes);
    }

    @Test
    void flatHandlerAndWrongContextFailClosed() {
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processCommand(command(payload("50"))));
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processTransaction(command(payload("50")),
                new SavingsTransactionExecutionContext(SavingsTransactionKind.WITHDRAWAL, SavingsTransactionOrigin.STAFF_API, maker)));
        verifyNoInteractions(writes, limits);
    }

    @Test
    void storedMakerAndPayloadWinOnApprovalAndCheckerIdentityIsPreserved() {
        var source = source();
        var configuration = mock(ConfigurationDomainService.class);
        when(configuration.isMakerCheckerEnabledForTask("CREATE_ACCOUNTTRANSFER")).thenReturn(true);
        var commands = commandService(configuration);
        commands.processCommandAndSaveResult(handler, command(payload("999999")), source, checker, true, (s, r) -> {});
        verify(writes).create(argThat(c -> c.bigDecimalValueOfParameterNamed("transferAmount").compareTo(new BigDecimal("50")) == 0));
        verify(source).markAsChecked(checker);
        verify(limits, never()).findByAppUserIdAndAuthorityTypeAndCurrencyCode(eq(8L), any(), any());
    }

    @Test
    void reducedAuthorityRejectsApprovalAndCannotQueue() {
        var source = source();
        configure("10", "40");
        var configuration = mock(ConfigurationDomainService.class);
        var commands = commandService(configuration);
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(payload("50")), source, checker, true, (s, r) -> {}));
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(payload("50")), source, maker, false, (s, r) -> {}));
        verify(source, never()).markAsAwaitingApproval();
        verify(source, never()).markAsChecked(any());
        verifyNoInteractions(writes, configuration);
    }

    @Test
    void refundUsesActualLoanCurrencyEvenIfRequestClaimsSavingsSource() {
        var loan = mock(Loan.class);
        when(loan.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, 0));
        when(loans.findOneWithNotFoundDetection(42L)).thenReturn(loan);
        var refund = new RefundByTransferCommandHandler(authority, writes);
        var context = new SavingsTransactionExecutionContext(SavingsTransactionKind.ACCOUNT_TRANSFER_REFUND,
                SavingsTransactionOrigin.STAFF_API, maker);
        assertThrows(GeneralPlatformDomainRuleException.class, () -> refund.processTransaction(command(payload("50")), context));
        verifyNoInteractions(writes, accounts);
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "USD")).thenReturn(Optional.of(
                new NsimbiUserMonetaryAuthority(7L, MonetaryAuthorityType.TRANSFER, "USD", new BigDecimal("10"), new BigDecimal("100"))));
        refund.processTransaction(command(payload("50")), context);
        verify(writes).refundByTransfer(any());
    }

    @Test
    void executionRetryKeepsStoredMakerOriginAndPayload() {
        var source = source();
        var commands = commandService(mock(ConfigurationDomainService.class));
        when(writes.create(any())).thenThrow(new ConcurrencyFailureException("retry")).thenReturn(CommandProcessingResult.empty());
        assertThrows(ConcurrencyFailureException.class,
                () -> commands.processCommandAndSaveResult(handler, command(payload("999")), source, checker, false, (s, r) -> {}));
        commands.processCommandAndSaveResult(handler, command(payload("999")), source, checker, false, (s, r) -> {});
        verify(writes, Mockito.times(2))
                .create(argThat(c -> c.bigDecimalValueOfParameterNamed("transferAmount").compareTo(new BigDecimal("50")) == 0));
        verify(limits, Mockito.times(2)).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX");
        verify(limits, never()).findByAppUserIdAndAuthorityTypeAndCurrencyCode(eq(8L), any(), any());
    }

    @Test
    void invalidRangeAndRemovedMakerAuthorityCannotBeOverriddenByChecker() {
        var invalid = new NsimbiUserMonetaryAuthority(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("10"),
                new BigDecimal("100"));
        ReflectionTestUtils.setField(invalid, "minimumAmount", new BigDecimal("200"));
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX"))
                .thenReturn(Optional.of(invalid));
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(8L, MonetaryAuthorityType.TRANSFER, "UGX")).thenReturn(Optional.of(
                new NsimbiUserMonetaryAuthority(8L, MonetaryAuthorityType.TRANSFER, "UGX", BigDecimal.ZERO, new BigDecimal("1000000"))));
        var source = source();
        var commands = commandService(mock(ConfigurationDomainService.class));
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(payload("50")), source, checker, true, (s, r) -> {}));
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX")).thenReturn(Optional.empty());
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(payload("50")), source, checker, true, (s, r) -> {}));
        verifyNoInteractions(writes);
        verify(source, never()).markAsChecked(any());
    }

    private CommandSource source() {
        CommandSource source = mock(CommandSource.class);
        when(source.getActionName()).thenReturn("CREATE");
        when(source.getEntityName()).thenReturn("ACCOUNTTRANSFER");
        when(source.getPermissionCode()).thenReturn("CREATE_ACCOUNTTRANSFER");
        when(source.getMaker()).thenReturn(maker);
        when(source.getCommandAsJson()).thenReturn(SavingsTransactionCommandEnvelope.encode(payload("50"),
                SavingsTransactionKind.ACCOUNT_TRANSFER, SavingsTransactionOrigin.STAFF_API));
        return source;
    }

    private CommandSourceService commandService(ConfigurationDomainService configuration) {
        return new CommandSourceService(configuration, mock(CommandSourceRepository.class), mock(ErrorHandler.class), json);
    }

    private void configure(String min, String max) {
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "UGX"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(7L, MonetaryAuthorityType.TRANSFER, "UGX",
                        new BigDecimal(min), new BigDecimal(max))));
    }

    private SavingsTransactionExecutionContext context() {
        return new SavingsTransactionExecutionContext(SavingsTransactionKind.ACCOUNT_TRANSFER, SavingsTransactionOrigin.STAFF_API, maker);
    }

    private String payload(String amount) {
        return "{\"fromAccountType\":2,\"fromAccountId\":42,\"transferAmount\":" + amount + ",\"locale\":\"en\"}";
    }

    private JsonCommand command(String payload) {
        return new JsonCommand(1L, json.parse(payload), json);
    }
}
