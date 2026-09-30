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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.domain.SavingsTransactionOrigin;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.data.AccountTransfersDetailDataValidator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDataValidator;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.handler.CreateStandingInstructionCommandHandler;
import org.apache.fineract.portfolio.account.handler.UpdateStandingInstructionCommandHandler;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class StandingInstructionAuthorityTest {

    private final FromJsonHelper json = new FromJsonHelper();
    private final NsimbiMonetaryAuthorityPolicyService policy = mock(NsimbiMonetaryAuthorityPolicyService.class);
    private final SavingsAccountRepositoryWrapper accounts = mock(SavingsAccountRepositoryWrapper.class);
    private final StandingInstructionRepository instructions = mock(StandingInstructionRepository.class);
    private final StandingInstructionWritePlatformService writes = mock(StandingInstructionWritePlatformService.class);
    private final AppUser maker = mock(AppUser.class);
    private AccountTransferAuthorityService authority;

    @BeforeEach
    void setup() {
        when(maker.getId()).thenReturn(7L);
        var account = mock(SavingsAccount.class);
        when(account.getCurrency()).thenReturn(new MonetaryCurrency("UGX", 2, 0));
        when(accounts.findOneWithNotFoundDetection(42L)).thenReturn(account);
        authority = new AccountTransferAuthorityService(policy, accounts, mock(LoanRepositoryWrapper.class),
                mock(AccountTransfersDataValidator.class), mock(StandingInstructionDataValidator.class), instructions);
    }

    @Test
    void deniedFixedInstructionCannotPersistEvenWhenDueNow() {
        var handler = new CreateStandingInstructionCommandHandler(authority, writes);
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> handler.processTransaction(fixed(), context(SavingsTransactionKind.STANDING_INSTRUCTION_CREATE)));
        verifyNoInteractions(writes);
    }

    @Test
    void allowedFixedInstructionUsesPrincipalAndSourceCurrency() {
        when(policy.allows(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("50"))).thenReturn(true);
        var handler = new CreateStandingInstructionCommandHandler(authority, writes);
        var command = fixed();
        handler.processTransaction(command, context(SavingsTransactionKind.STANDING_INSTRUCTION_CREATE));
        verify(policy).allows(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("50"));
        verify(writes).create(command);
    }

    @Test
    void existingDuesSuspensionUsesLockedStateWithoutMonetaryAuthority() {
        var instruction = mock(AccountTransferStandingInstruction.class);
        when(instruction.instructionType()).thenReturn(2);
        when(instructions.findByIdForUpdate(17L)).thenReturn(Optional.of(instruction));
        JsonCommand command = spy(new JsonCommand(1L, json.parse("{\"status\":2}"), json));
        doReturn(17L).when(command).entityId();
        var handler = new UpdateStandingInstructionCommandHandler(authority, writes);
        handler.processTransaction(command, context(SavingsTransactionKind.STANDING_INSTRUCTION_UPDATE));
        verify(instructions).findByIdForUpdate(17L);
        verify(writes).update(eq(17L), any());
        verifyNoInteractions(policy);
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"priority\":2}", "{\"status\":1}", "{\"recurrenceInterval\":2}", "{\"validTill\":\"30 September 2026\"}" })
    void fixedMaterialChangesAuthorizeStoredPrincipalBeforeWriting(String payload) {
        var instruction = fixedInstruction();
        when(instructions.findByIdForUpdate(17L)).thenReturn(Optional.of(instruction));
        JsonCommand command = update(payload);
        var handler = new UpdateStandingInstructionCommandHandler(authority, writes);
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> handler.processTransaction(command, context(SavingsTransactionKind.STANDING_INSTRUCTION_UPDATE)));
        verify(policy).allows(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("50"));
        verifyNoInteractions(writes);
    }

    @Test
    void changedFixedAmountIsAuthorizedInsteadOfOldAmount() {
        var instruction = fixedInstruction();
        when(instructions.findByIdForUpdate(17L)).thenReturn(Optional.of(instruction));
        JsonCommand command = update("{\"amount\":100,\"locale\":\"en\"}");
        var handler = new UpdateStandingInstructionCommandHandler(authority, writes);
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> handler.processTransaction(command, context(SavingsTransactionKind.STANDING_INSTRUCTION_UPDATE)));
        verify(policy).allows(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("100"));
        verifyNoInteractions(writes);
    }

    @Test
    void pendingDuesApprovalRejectsBeforeSetupWriterOrApprovalStateChange() {
        var validator = new StandingInstructionDataValidator(json, mock(AccountTransfersDetailDataValidator.class));
        var guarded = new AccountTransferAuthorityService(policy, accounts, mock(LoanRepositoryWrapper.class),
                mock(AccountTransfersDataValidator.class), validator, instructions);
        var source = source("{\"instructionType\":2}");
        var configuration = mock(ConfigurationDomainService.class);
        var commands = commandService(configuration);
        assertThrows(PlatformApiDataValidationException.class,
                () -> commands.processCommandAndSaveResult(new CreateStandingInstructionCommandHandler(guarded, writes), fixed(), source,
                        mock(AppUser.class), true, (saved, result) -> {}));
        verifyNoInteractions(writes, policy, configuration);
        Mockito.verify(source, Mockito.never()).markAsChecked(any());
    }

    @Test
    void pendingFixedSetupRollsBackAndApprovalAloneCommits() throws Exception {
        when(policy.allows(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("50"))).thenReturn(true);
        var source = source(fixed().json());
        var configuration = mock(ConfigurationDomainService.class);
        when(configuration.isMakerCheckerEnabledForTask("CREATE_STANDINGINSTRUCTION")).thenReturn(true);
        var connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        var dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        var manager = new DataSourceTransactionManager(dataSource);
        var proxy = new ProxyFactory(commandService(configuration));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager,
                new AnnotationTransactionAttributeSource()));
        var commands = (CommandSourceService) proxy.getProxy();
        var committed = new ArrayList<String>();
        when(writes.create(any())).thenAnswer(invocation -> {
            Assertions.assertTrue(
                    TransactionSynchronizationManager.isActualTransactionActive());
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            committed.add("active fixed instruction");
                        }
                    });
            return CommandProcessingResult.empty();
        });
        var handler = new CreateStandingInstructionCommandHandler(authority, writes);
        assertThrows(RollbackTransactionNotApprovedException.class,
                () -> commands.processCommandAndSaveResult(handler, fixed(), source, maker, false, (saved, result) -> {}));
        Assertions.assertTrue(committed.isEmpty());
        verify(connection).rollback();
        Mockito.verify(connection, Mockito.never()).commit();
        verify(source).markAsAwaitingApproval();

        var checker = mock(AppUser.class);
        commands.processCommandAndSaveResult(handler, fixed(), source, checker, true, (saved, result) -> {});
        Assertions.assertEquals(List.of("active fixed instruction"), committed);
        verify(connection).commit();
        verify(source).markAsChecked(checker);
        verify(policy, Mockito.times(2)).allows(7L, MonetaryAuthorityType.TRANSFER, "UGX", new BigDecimal("50"));
    }

    private CommandSource source(String payload) {
        var source = mock(CommandSource.class);
        when(source.getActionName()).thenReturn("CREATE");
        when(source.getEntityName()).thenReturn("STANDINGINSTRUCTION");
        when(source.getPermissionCode()).thenReturn("CREATE_STANDINGINSTRUCTION");
        when(source.getMaker()).thenReturn(maker);
        when(source.getCommandAsJson()).thenReturn(SavingsTransactionCommandEnvelope.encode(payload,
                SavingsTransactionKind.STANDING_INSTRUCTION_CREATE, SavingsTransactionOrigin.STAFF_API));
        return source;
    }

    private CommandSourceService commandService(ConfigurationDomainService configuration) {
        return new CommandSourceService(configuration, mock(CommandSourceRepository.class), mock(ErrorHandler.class), json);
    }

    private AccountTransferStandingInstruction fixedInstruction() {
        var instruction = mock(AccountTransferStandingInstruction.class);
        var details = mock(AccountTransferDetails.class);
        when(instruction.instructionType()).thenReturn(1);
        when(instruction.amount()).thenReturn(new BigDecimal("50"));
        when(instruction.transferDetails()).thenReturn(details);
        var source = accounts.findOneWithNotFoundDetection(42L);
        when(details.fromSavingsAccount()).thenReturn(source);
        return instruction;
    }

    private JsonCommand update(String payload) {
        JsonCommand command = spy(new JsonCommand(1L, json.parse(payload), json));
        doReturn(17L).when(command).entityId();
        return command;
    }

    private JsonCommand fixed() {
        return new JsonCommand(1L, json.parse("{\"fromAccountType\":2,\"fromAccountId\":42,\"amount\":50,"
                + "\"instructionType\":1,\"status\":1,\"locale\":\"en\",\"validFrom\":\"29 September 2026\"}"), json);
    }

    private SavingsTransactionExecutionContext context(SavingsTransactionKind kind) {
        return new SavingsTransactionExecutionContext(kind, SavingsTransactionOrigin.STAFF_API, maker);
    }
}
