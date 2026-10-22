/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.LoanCommandExecutionContext;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanMonetaryAuthorityService implements LoanApprovalAuthority {

    private final NsimbiMonetaryAuthorityPolicyService monetaryAuthority;

    @Override
    public void requireApproval(LoanCommandExecutionContext context, Loan loan, BigDecimal amount) {
        require(context, MonetaryAuthorityType.LOAN_APPROVAL, loan, amount);
    }

    /** Read the gross amount before disbursement changes managed entities or publishes events. */
    public void requireDisbursement(LoanCommandExecutionContext context, Loan loan, JsonCommand command, boolean createArtificialTranche) {
        BigDecimal amount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalDisbursedParameterName);
        if (amount == null) {
            Collection<LoanDisbursementDetails> details = loan.fetchUndisbursedDetail();
            if (!details.isEmpty()) {
                amount = details.stream().map(LoanDisbursementDetails::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
            } else if (createArtificialTranche && loan.loanProduct().isDisallowExpectedDisbursements()) {
                amount = loan.getDisbursedAmount();
            } else {
                amount = loan.getPrincipal().getAmount();
            }
        }
        require(context, MonetaryAuthorityType.DISBURSEMENT, loan, amount);
    }

    public void require(LoanCommandExecutionContext context, MonetaryAuthorityType type, Loan loan, BigDecimal amount) {
        AppUser maker = context == null ? null : context.requireMaker();
        if (maker == null) {
            throw LoanCommandExecutionContext.untrustedOrigin();
        }
        if (!loan.getOffice().getHierarchy().startsWith(maker.getOffice().getHierarchy())) {
            throw new NoAuthorizationException("The user doesn't have enough permissions to access the resource.");
        }
        String currency = loan.getCurrency().getCode();
        if (!monetaryAuthority.allows(maker.getId(), type, currency, amount)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.monetary.authority.denied",
                    "The original submitter does not have the required monetary authority for this amount and loan currency.");
        }
    }
}
