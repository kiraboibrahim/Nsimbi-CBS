/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.portfolio.loanaccount.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.commands.domain.LoanCommandExecutionContext;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanApprovedAmountHistoryRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApprovedAmountValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;

class LoanApprovedAmountAuthorityTest {

    private final LoanAssembler assembler = mock(LoanAssembler.class);
    private final LoanApprovalAuthority authority = mock(LoanApprovalAuthority.class);
    private final LoanApprovedAmountHistoryRepository history = mock(LoanApprovedAmountHistoryRepository.class);
    private final BusinessEventNotifierService events = mock(BusinessEventNotifierService.class);
    private final LoanApprovedAmountWritePlatformServiceImpl service = new LoanApprovedAmountWritePlatformServiceImpl(assembler, authority,
            mock(LoanApprovedAmountValidator.class), history, events);
    private final Loan loan = mock(Loan.class);
    private final JsonCommand command = mock(JsonCommand.class);
    private final LoanCommandExecutionContext context = new LoanCommandExecutionContext(mock(AppUser.class));

    @Test
    void deniedApprovedAmountChangeDoesNotMutateLoanOrPublishEvent() {
        when(assembler.assembleFrom(1L)).thenReturn(loan);
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.TEN);
        when(command.bigDecimalValueOfParameterNamed("amount")).thenReturn(BigDecimal.valueOf(20));
        deny(BigDecimal.valueOf(20));

        assertThatThrownBy(() -> service.modifyLoanApprovedAmount(1L, command, context))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(loan, never()).setApprovedPrincipal(any());
        verifyNoInteractions(history, events);
    }

    @Test
    void availableAmountChangeChecksTotalApprovalBeforeMutation() {
        when(assembler.assembleFrom(1L)).thenReturn(loan);
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(100));
        LoanSummary summary = mock(LoanSummary.class);
        when(loan.getSummary()).thenReturn(summary);
        when(summary.getTotalPrincipal()).thenReturn(BigDecimal.valueOf(60));
        when(loan.getDisbursementDetails()).thenReturn(List.of());
        when(command.bigDecimalValueOfParameterNamed("amount")).thenReturn(BigDecimal.valueOf(50));
        deny(BigDecimal.valueOf(110));

        assertThatThrownBy(() -> service.modifyLoanAvailableDisbursementAmount(1L, command, context))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(authority).requireApproval(context, loan, BigDecimal.valueOf(110));
        verify(loan, never()).setApprovedPrincipal(any());
        verifyNoInteractions(history, events);
    }

    @Test
    void directAmountChangesRequireExecutionContext() {
        assertThatThrownBy(() -> service.modifyLoanApprovedAmount(1L, command)).isInstanceOf(GeneralPlatformDomainRuleException.class);
        assertThatThrownBy(() -> service.modifyLoanAvailableDisbursementAmount(1L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verifyNoInteractions(assembler, authority, history, events);
    }

    private void deny(BigDecimal amount) {
        doThrow(new GeneralPlatformDomainRuleException("denied", "Denied")).when(authority).requireApproval(eq(context), eq(loan),
                eq(amount));
    }
}
