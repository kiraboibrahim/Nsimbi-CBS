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

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.domain.DepositAccountClosurePlan;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositClosureAuthorityService {

    private final EntityManager entityManager;
    private final ConfigurationDomainService configuration;
    private final NsimbiMonetaryAuthorityPolicyService authority;

    public DepositAccountClosurePlan prepare(SavingsAccount account, JsonCommand command, SavingsTransactionExecutionContext context,
            boolean premature) {
        if (DepositAccountOnClosureType
                .fromInt(command.integerValueOfParameterNamed("onAccountClosureId")) != DepositAccountOnClosureType.TRANSFER_TO_SAVINGS)
            return null;
        boolean fixed = account instanceof FixedDepositAccount;
        var kind = fixed ? (premature ? SavingsTransactionKind.FIXED_DEPOSIT_PREMATURE_CLOSE : SavingsTransactionKind.FIXED_DEPOSIT_CLOSE)
                : (premature ? SavingsTransactionKind.RECURRING_DEPOSIT_PREMATURE_CLOSE : SavingsTransactionKind.RECURRING_DEPOSIT_CLOSE);
        if (context == null) throw SavingsTransactionCommandEnvelope.untrustedOrigin(kind);
        context.requireKind(kind);
        entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE);
        var closeDate = command.localDateValueOfParameterNamed("closedOnDate");
        boolean atPeriodEnd = configuration.isSavingsInterestPostingAtCurrentPeriodEnd();
        int financialYear = configuration.retrieveFinancialYearBeginningMonth();
        var plan = fixed ? ((FixedDepositAccount) account).planClosureInterest(closeDate, premature, atPeriodEnd, financialYear)
                : ((RecurringDepositAccount) account).planClosureInterest(closeDate, premature, atPeriodEnd, financialYear);
        if (!authority.allows(context.maker().getId(), MonetaryAuthorityType.TRANSFER, plan.currency(), plan.netProceeds())) {
            throw new GeneralPlatformDomainRuleException("error.msg.accounttransfer.monetary.authority.denied",
                    "The original submitter does not have TRANSFER authority for the net closure proceeds in source currency.");
        }
        plan.validateCurrent(account);
        return plan;
    }
}
