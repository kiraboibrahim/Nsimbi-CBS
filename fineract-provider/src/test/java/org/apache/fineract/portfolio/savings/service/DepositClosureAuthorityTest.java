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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.commands.domain.*;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.nsimbi.userroles.domain.*;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.*;
import org.apache.fineract.portfolio.savings.handler.CloseFixedDepositAccountCommandHandler;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class DepositClosureAuthorityTest {

    private final FromJsonHelper json = new FromJsonHelper();
    @Mock
    private PlatformSecurityContext security;
    @Mock
    private DepositAccountAssembler assembler;
    @Mock
    private DepositAccountTransactionDataValidator validator;
    @Mock
    private PaymentDetailWritePlatformService payment;
    @Mock
    private DepositAccountDomainService domain;
    @Mock
    private NoteRepository notes;
    @InjectMocks
    private DepositAccountWritePlatformServiceJpaRepositoryImpl writer;
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ConfigurationDomainService configuration = mock(ConfigurationDomainService.class);
    private final NsimbiUserMonetaryAuthorityRepository limits = mock(NsimbiUserMonetaryAuthorityRepository.class);
    private final FixedDepositAccount account = mock(FixedDepositAccount.class);
    private final DepositAccountClosurePlan plan = mock(DepositAccountClosurePlan.class);
    private final AppUser maker = mock(AppUser.class);
    private final AppUser checker = mock(AppUser.class);
    private CloseFixedDepositAccountCommandHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(maker.getId()).thenReturn(7L);
        when(checker.getId()).thenReturn(8L);
        when(security.authenticatedUser()).thenReturn(checker);
        when(configuration.retrieveFinancialYearBeginningMonth()).thenReturn(1);
        when(assembler.assembleFrom(any(), eq(DepositAccountType.FIXED_DEPOSIT))).thenReturn(account);
        when(account.planClosureInterest(any(), eq(false), eq(false), eq(1))).thenReturn(plan);
        when(plan.currency()).thenReturn("USD");
        when(plan.netProceeds()).thenReturn(new BigDecimal("1004.50"));
        var authority = new DepositClosureAuthorityService(entityManager, configuration, new NsimbiMonetaryAuthorityPolicyService(limits));
        ReflectionTestUtils.setField(writer, "closureAuthority", authority);
        handler = new CloseFixedDepositAccountCommandHandler(writer);
        limit("1000", "1100");
    }

    @ParameterizedTest
    @CsvSource({ "1004.50,1100,true", "1000,1004.50,true", "1004.51,1100,false", "1000,1004.49,false" })
    void netProceedsInclusiveBoundsAndZeroFinancialEffectsOnDenial(String min, String max, boolean allowed) {
        limit(min, max);
        if (allowed) {
            handler.processTransaction(command(200), context());
            var order = inOrder(entityManager, account, limits, plan, payment, domain);
            order.verify(entityManager).lock(account, LockModeType.PESSIMISTIC_WRITE);
            order.verify(account).planClosureInterest(any(), eq(false), eq(false), eq(1));
            order.verify(limits).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "USD");
            order.verify(plan).validateCurrent(account);
            order.verify(payment).createAndPersistPaymentDetail(any(), anyMap());
            order.verify(domain).handleFDAccountClosure(eq(account), any(), eq(checker), any(), anyMap(), same(plan));
        } else {
            assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processTransaction(command(200), context()));
            verifyNoInteractions(payment, domain, notes);
        }
    }

    @Test
    void nonTransferDoesNotCalculateOrRequireTransferAuthority() {
        handler.processTransaction(command(100), context());
        verifyNoInteractions(limits, entityManager, plan);
        verify(account, never()).planClosureInterest(any(), anyBoolean(), anyBoolean(), anyInt());
        verify(domain).handleFDAccountClosure(eq(account), any(), eq(checker), any(), anyMap(), isNull());
    }

    @Test void approvalUsesMakerAndRecalculatesCurrentProceeds() {
        when(configuration.isMakerCheckerEnabledForTask("CLOSE_FIXEDDEPOSITACCOUNT")).thenReturn(true);
        var source = source();
        var commands = commandService();
        commands.processCommandAndSaveResult(handler, command(100), source, checker, true, (s,r) -> {});
        when(plan.netProceeds()).thenReturn(new BigDecimal("1200"));
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(100), source, checker, true, (s,r) -> {}));
        verify(account, times(2)).planClosureInterest(any(), eq(false), eq(false), eq(1));
        verify(limits, times(2)).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "USD");
        verify(limits, never()).findByAppUserIdAndAuthorityTypeAndCurrencyCode(eq(8L), any(), any());
        verify(payment, times(1)).createAndPersistPaymentDetail(any(), anyMap());
        verify(source).markAsChecked(checker);
    }

    @Test void reducedMakerLimitCannotBeOverriddenByCheckerAndNeverQueues() {
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(8L, MonetaryAuthorityType.TRANSFER, "USD"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(8L, MonetaryAuthorityType.TRANSFER,"USD",BigDecimal.ZERO,new BigDecimal("999999"))));
        limit("0", "1000");
        var source = source();
        var commands = commandService();
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(200), source, checker, true, (s,r) -> {}));
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.processCommandAndSaveResult(handler, command(200), source, maker, false, (s,r) -> {}));
        verifyNoInteractions(payment, domain, notes);
        verify(source, never()).markAsChecked(any());
        verify(source, never()).markAsAwaitingApproval();
    }

    @Test
    void retryRecalculatesAndRetainsPersistedMakerAndPayload() {
        var source = source();
        var commands = commandService();
        when(domain.handleFDAccountClosure(eq(account), any(), any(), any(), anyMap(), same(plan)))
                .thenThrow(new org.springframework.dao.ConcurrencyFailureException("retry")).thenReturn(1L);
        assertThrows(org.springframework.dao.ConcurrencyFailureException.class,
                () -> commands.processCommandAndSaveResult(handler, command(100), source, checker, false, (s, r) -> {}));
        commands.processCommandAndSaveResult(handler, command(100), source, checker, false, (s, r) -> {});
        verify(account, times(2)).planClosureInterest(any(), eq(false), eq(false), eq(1));
        verify(limits, times(2)).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "USD");
        verify(domain, times(2)).handleFDAccountClosure(eq(account), any(), eq(checker),
                argThat(c -> c.integerValueOfParameterNamed("onAccountClosureId") == 200), anyMap(), same(plan));
    }

    @ParameterizedTest
    @CsvSource({ "true,false,FIXED_DEPOSIT_CLOSE", "true,true,FIXED_DEPOSIT_PREMATURE_CLOSE", "false,false,RECURRING_DEPOSIT_CLOSE",
            "false,true,RECURRING_DEPOSIT_PREMATURE_CLOSE" })
    void allFourModesRequireTheirOriginalMakerContext(boolean fixed, boolean premature, SavingsTransactionKind kind) {
        SavingsAccount sourceAccount;
        if (fixed) {
            sourceAccount = account;
            when(account.planClosureInterest(any(), eq(premature), eq(false), eq(1))).thenReturn(plan);
        } else {
            var recurring = mock(RecurringDepositAccount.class);
            when(recurring.planClosureInterest(any(), eq(premature), eq(false), eq(1))).thenReturn(plan);
            sourceAccount = recurring;
        }
        var service = new DepositClosureAuthorityService(entityManager, configuration, new NsimbiMonetaryAuthorityPolicyService(limits));
        var makerContext = new SavingsTransactionExecutionContext(kind, SavingsTransactionOrigin.STAFF_API, maker);
        assertThat(service.prepare(sourceAccount, command(200), makerContext, premature)).isSameAs(plan);
        verify(entityManager).lock(sourceAccount, LockModeType.PESSIMISTIC_WRITE);
        verify(limits).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "USD");
        assertThrows(GeneralPlatformDomainRuleException.class, () -> service.prepare(sourceAccount, command(200), null, premature));
    }

    @Test void missingMakerAuthorityDeniesBeforeFinancialWrites() {
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER, "USD"))
                .thenReturn(Optional.empty());
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processTransaction(command(200), context()));
        verifyNoInteractions(payment, domain, notes);
    }

    private void limit(String min,String max) {
        when(limits.findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.TRANSFER,"USD"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(7L,MonetaryAuthorityType.TRANSFER,"USD",new BigDecimal(min),new BigDecimal(max))));
    }

    private String payload(int type) {
        return "{\"closedOnDate\":\"06 January 2025\",\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\",\"onAccountClosureId\":" + type
                + ",\"toSavingsAccountId\":99}";
    }

    private JsonCommand command(int type) {
        return new JsonCommand(1L, json.parse(payload(type)), json);
    }

    private SavingsTransactionExecutionContext context() {
        return new SavingsTransactionExecutionContext(SavingsTransactionKind.FIXED_DEPOSIT_CLOSE, SavingsTransactionOrigin.STAFF_API,
                maker);
    }

    private CommandSource source() {
        var source = mock(CommandSource.class);
        when(source.getActionName()).thenReturn("CLOSE");
        when(source.getEntityName()).thenReturn("FIXEDDEPOSITACCOUNT");
        when(source.getPermissionCode()).thenReturn("CLOSE_FIXEDDEPOSITACCOUNT");
        when(source.getMaker()).thenReturn(maker);
        when(source.getCommandAsJson()).thenReturn(SavingsTransactionCommandEnvelope.encode(payload(200),
                SavingsTransactionKind.FIXED_DEPOSIT_CLOSE, SavingsTransactionOrigin.STAFF_API));
        return source;
    }

    private CommandSourceService commandService() {
        return new CommandSourceService(configuration, mock(CommandSourceRepository.class), mock(ErrorHandler.class), json);
    }
}
