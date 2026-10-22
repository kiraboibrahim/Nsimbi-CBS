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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.fineract.commands.domain.LoanCommandExecutionContext;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanDisbursementAuthorityOrderingTest {

    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private LoanMonetaryAuthorityService authority;
    @Mock
    private LoanTransactionValidator validator;
    @Mock
    private PaymentDetailWritePlatformService payments;
    @Mock
    private BusinessEventNotifierService events;
    @Mock
    private LoanTransactionRepository transactions;
    @Mock
    private LoanJournalEntryPoster journals;
    @Mock
    private AccountTransfersWritePlatformService transfers;
    @InjectMocks
    private LoanWritePlatformServiceJpaRepositoryImpl service;

    @ParameterizedTest
    @CsvSource({ "false,false", "true,false", "false,true" })
    void deniedAlternateDisbursementHasNoFinancialSideEffects(boolean toSavings, boolean withoutAutoPayment) {
        Loan loan = mock(Loan.class);
        JsonCommand command = mock(JsonCommand.class);
        LoanCommandExecutionContext context = new LoanCommandExecutionContext(mock(AppUser.class));
        when(loanAssembler.assembleFrom(1L)).thenReturn(loan);
        deny();
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> service.disburseLoan(1L, command, toSavings, withoutAutoPayment, context));
        verify(authority).requireDisbursement(context, loan, command, true);
        verifyNoInteractions(loan, payments, events, transactions, journals, transfers);
    }

    @Test
    void bulkDenialPrecedesFinancialSideEffectsAndUsesSuppliedMaker() {
        Loan loan = mock(Loan.class);
        JsonCommand command = mock(JsonCommand.class);
        CollectionSheetBulkDisbursalCommand bulk = mock(CollectionSheetBulkDisbursalCommand.class);
        SingleDisbursalCommand child = mock(SingleDisbursalCommand.class);
        when(child.getLoanId()).thenReturn(1L);
        when(bulk.getDisburseTransactions()).thenReturn(new SingleDisbursalCommand[] { child });
        when(loanAssembler.assembleFrom(1L)).thenReturn(loan);
        LoanCommandExecutionContext context = new LoanCommandExecutionContext(mock(AppUser.class));
        deny();
        assertThrows(GeneralPlatformDomainRuleException.class, () -> service.bulkLoanDisbursal(command, bulk, false, context));
        verify(authority).requireDisbursement(context, loan, command, false);
        verifyNoInteractions(loan, payments, events, transactions, journals, transfers);
    }

    private void deny() {
        doThrow(new GeneralPlatformDomainRuleException("denied", "Denied")).when(authority).requireDisbursement(any(), any(), any(),
                anyBoolean());
    }
}
