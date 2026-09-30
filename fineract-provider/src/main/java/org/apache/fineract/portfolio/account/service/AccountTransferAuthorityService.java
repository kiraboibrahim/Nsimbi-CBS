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

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDataValidator;
import org.apache.fineract.portfolio.account.domain.StandingInstructionDuesPolicy;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.exception.StandingInstructionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.stereotype.Service;

/** Monetary preflight only: operation permissions and financial eligibility stay in their existing services. */
@Service
@RequiredArgsConstructor
public class AccountTransferAuthorityService {

    private final NsimbiMonetaryAuthorityPolicyService monetaryAuthority;
    private final SavingsAccountRepositoryWrapper savingsAccounts;
    private final LoanRepositoryWrapper loans;
    private final AccountTransfersDataValidator transferValidator;
    private final StandingInstructionDataValidator instructionValidator;
    private final StandingInstructionRepository instructions;

    public void authorize(JsonCommand command, SavingsTransactionExecutionContext context, SavingsTransactionKind expected) {
        if (context == null) {
            throw SavingsTransactionCommandEnvelope.untrustedOrigin(expected);
        }
        context.requireKind(expected);
        BigDecimal principal;
        String currency;
        if (expected == SavingsTransactionKind.STANDING_INSTRUCTION_UPDATE) {
            instructionValidator.validateForUpdate(command);
            var instruction = instructions.findByIdForUpdate(command.entityId())
                    .orElseThrow(() -> new StandingInstructionNotFoundException(command.entityId()));
            StandingInstructionDuesPolicy.validateUpdate(instruction.instructionType(), command);
            if (StandingInstructionType.DUES.getValue().equals(instruction.instructionType())) {
                return; // Only suspension can reach this point. It creates no new financial exposure.
            }
            principal = command.hasParameter("amount") ? command.bigDecimalValueOfParameterNamed("amount") : instruction.amount();
            var details = instruction.transferDetails();
            currency = details.fromSavingsAccount() != null ? details.fromSavingsAccount().getCurrency().getCode()
                    : details.fromLoanAccount().getCurrency().getCode();
        } else {
            if (expected == SavingsTransactionKind.STANDING_INSTRUCTION_CREATE) {
                instructionValidator.validateForCreate(command);
                principal = command.bigDecimalValueOfParameterNamed("amount");
            } else {
                transferValidator.validate(command);
                principal = command.bigDecimalValueOfParameterNamed("transferAmount");
            }
            Integer sourceType = expected == SavingsTransactionKind.ACCOUNT_TRANSFER_REFUND ? PortfolioAccountType.LOAN.getValue()
                    : command.integerValueSansLocaleOfParameterNamed("fromAccountType");
            Long sourceId = command.longValueOfParameterNamed("fromAccountId");
            if (PortfolioAccountType.SAVINGS.getValue().equals(sourceType)) {
                currency = savingsAccounts.findOneWithNotFoundDetection(sourceId).getCurrency().getCode();
            } else if (PortfolioAccountType.LOAN.getValue().equals(sourceType)) {
                currency = loans.findOneWithNotFoundDetection(sourceId).getCurrency().getCode();
            } else {
                throw new GeneralPlatformDomainRuleException("error.msg.accounttransfer.invalid.source.type",
                        "The transfer source must be a savings or loan account.");
            }
        }
        if (!monetaryAuthority.allows(context.maker().getId(), MonetaryAuthorityType.TRANSFER, currency, principal)) {
            throw new GeneralPlatformDomainRuleException("error.msg.accounttransfer.monetary.authority.denied",
                    "The original submitter does not have TRANSFER monetary authority for this principal and source-account currency.");
        }
    }
}
