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
package org.apache.fineract.portfolio.savings.domain;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.service.SavingsAccountPostInterestService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountTransfersService;
import org.junit.jupiter.api.Test;

class DepositClosureWithdrawalTest {

    @Test
    void appliedClosureSkipsInterestCalculationAndPostingButRetainsControlsAndAccounting() {
        var date = LocalDate.of(2025, 1, 6);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));
        try {
            var repository = mock(SavingsAccountRepositoryWrapper.class);
            var transactions = mock(SavingsAccountTransactionRepository.class);
            var journals = mock(JournalEntryWritePlatformService.class);
            var events = mock(BusinessEventNotifierService.class);
            var interest = mock(SavingsAccountPostInterestService.class);
            var domain = new SavingsAccountDomainServiceJpa(repository, transactions, mock(ApplicationCurrencyRepositoryWrapper.class),
                    journals, mock(ConfigurationDomainService.class), mock(PlatformSecurityContext.class),
                    mock(DepositAccountOnHoldTransactionRepository.class), events, interest, mock(SavingsAccountTransfersService.class));
            var account = mock(SavingsAccount.class);
            var withdrawal = mock(SavingsAccountTransaction.class);
            var receipt = mock(DepositAccountClosurePlan.Applied.class);
            when(account.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, 0));
            when(account.getOnHoldFunds()).thenReturn(BigDecimal.ZERO);
            when(account.withdraw(any(), anyBoolean(), anyBoolean(), any(), anyString())).thenReturn(withdrawal);
            var amount = new BigDecimal("1004.50");
            var flags = new SavingsTransactionBooleanValues(true, false, false, false, false);
            domain.handleWithdrawal(account, null, date, amount, null, flags, false, receipt);
            var order = inOrder(receipt, account, transactions, journals, events);
            order.verify(receipt).validate(account, amount, date);
            order.verify(account).validateForAccountBlock();
            order.verify(account).validateForDebitBlock();
            order.verify(account).withdraw(any(), eq(false), eq(false), any(), anyString());
            order.verify(receipt).updateWithdrawalBalances(account);
            order.verify(account).validateAccountBalanceConstraints(eq(amount), eq(false), isNull(), eq(false), eq(false));
            order.verify(transactions).saveAndFlush(withdrawal);
            order.verify(journals).createJournalEntriesForSavings(any());
            order.verify(events).notifyPostBusinessEvent(any());
            verifyNoInteractions(interest);
            verify(account, never()).calculateInterestUsing(any(), any(), anyBoolean(), anyBoolean(), any(), any(), anyBoolean(),
                    anyBoolean());
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }
}
