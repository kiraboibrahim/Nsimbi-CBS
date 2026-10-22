/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.portfolio.loanaccount.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthorityRepository;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanMonetaryAuthorityServiceTest {

    private final NsimbiMonetaryAuthorityPolicyService policy = mock(NsimbiMonetaryAuthorityPolicyService.class);
    private final LoanMonetaryAuthorityService service = new LoanMonetaryAuthorityService(policy);
    private final AppUser maker = mock(AppUser.class);
    private final Loan loan = mock(Loan.class);
    private final Office makerOffice = mock(Office.class);
    private final Office loanOffice = mock(Office.class);
    private final MonetaryCurrency currency = mock(MonetaryCurrency.class);
    private final LoanCommandExecutionContext context = new LoanCommandExecutionContext(maker);

    @BeforeEach
    void setUp() {
        when(maker.getId()).thenReturn(7L);
        when(maker.getOffice()).thenReturn(makerOffice);
        when(loan.getOffice()).thenReturn(loanOffice);
        when(loan.getCurrency()).thenReturn(currency);
        when(makerOffice.getHierarchy()).thenReturn(".1.2.");
        when(loanOffice.getHierarchy()).thenReturn(".1.2.4.");
    }

    @Test
    void permitsApprovalWithinAmountCurrencyAndOfficeHierarchy() {
        BigDecimal amount = new BigDecimal("1000000");
        when(currency.getCode()).thenReturn("UGX");
        when(policy.allows(7L, MonetaryAuthorityType.LOAN_APPROVAL, "UGX", amount)).thenReturn(true);

        assertThatCode(() -> service.require(context, MonetaryAuthorityType.LOAN_APPROVAL, loan, amount)).doesNotThrowAnyException();

        verify(policy).allows(7L, MonetaryAuthorityType.LOAN_APPROVAL, "UGX", amount);
    }

    @Test
    void deniesDisbursementThatExceedsAuthority() {
        BigDecimal amount = new BigDecimal("1000001");
        when(currency.getCode()).thenReturn("UGX");

        assertThatThrownBy(() -> service.require(context, MonetaryAuthorityType.DISBURSEMENT, loan, amount)).isInstanceOfSatisfying(
                GeneralPlatformDomainRuleException.class,
                exception -> assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.monetary.authority.denied"));

        verify(policy).allows(7L, MonetaryAuthorityType.DISBURSEMENT, "UGX", amount);
    }

    @Test
    void usesLoanCurrencyForAuthorityLookup() {
        BigDecimal amount = new BigDecimal("100");
        when(currency.getCode()).thenReturn("USD");

        assertThatThrownBy(() -> service.require(context, MonetaryAuthorityType.DISBURSEMENT, loan, amount))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);

        verify(policy).allows(7L, MonetaryAuthorityType.DISBURSEMENT, "USD", amount);
    }

    @Test
    void deniesLoanOutsideMakersOfficeHierarchyBeforeLimitLookup() {
        when(loanOffice.getHierarchy()).thenReturn(".1.3.");

        assertThatThrownBy(() -> service.require(context, MonetaryAuthorityType.LOAN_APPROVAL, loan, BigDecimal.TEN))
                .isInstanceOf(NoAuthorizationException.class);

        verifyNoInteractions(policy);
    }

    @Test
    void deniesMissingExecutionContext() {
        assertThatThrownBy(() -> service.require(null, MonetaryAuthorityType.LOAN_APPROVAL, loan, BigDecimal.TEN))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verifyNoInteractions(policy);
    }

    @Test
    void deniesMissingOriginalMaker() {
        assertThatThrownBy(
                () -> service.require(new LoanCommandExecutionContext(null), MonetaryAuthorityType.DISBURSEMENT, loan, BigDecimal.TEN))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verifyNoInteractions(policy);
    }

    @Test
    void missingAuthorityRecordIsDeniedByExistingPolicy() {
        var repository = mock(NsimbiUserMonetaryAuthorityRepository.class);
        var actualPolicy = new NsimbiMonetaryAuthorityPolicyService(repository);
        when(currency.getCode()).thenReturn("UGX");
        var actualService = new LoanMonetaryAuthorityService(actualPolicy);
        assertThatThrownBy(() -> actualService.require(context, MonetaryAuthorityType.DISBURSEMENT, loan, BigDecimal.TEN))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(repository).findByAppUserIdAndAuthorityTypeAndCurrencyCode(7L, MonetaryAuthorityType.DISBURSEMENT, "UGX");
    }

    @Test
    void preflightUsesExplicitGrossPrincipal() {
        var command = mock(JsonCommand.class);
        when(command.bigDecimalValueOfParameterNamed("transactionAmount")).thenReturn(BigDecimal.TEN);
        when(currency.getCode()).thenReturn("UGX");
        assertThatThrownBy(() -> service.requireDisbursement(context, loan, command, true))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(policy).allows(7L, MonetaryAuthorityType.DISBURSEMENT, "UGX", BigDecimal.TEN);
    }

    @Test
    void preflightSumsNextUndisbursedTranchesWhenAmountOmitted() {
        var command = mock(JsonCommand.class);
        var first = mock(LoanDisbursementDetails.class);
        var second = mock(LoanDisbursementDetails.class);
        when(first.getPrincipal()).thenReturn(BigDecimal.TEN);
        when(second.getPrincipal()).thenReturn(BigDecimal.valueOf(20));
        when(loan.fetchUndisbursedDetail()).thenReturn(List.of(first, second));
        when(currency.getCode()).thenReturn("UGX");
        assertThatThrownBy(() -> service.requireDisbursement(context, loan, command, false))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(policy).allows(7L, MonetaryAuthorityType.DISBURSEMENT, "UGX", BigDecimal.valueOf(30));
    }

    @Test
    void preflightUsesPrincipalWithoutUndisbursedTranches() {
        var command = mock(JsonCommand.class);
        var principal = mock(Money.class);
        when(loan.getPrincipal()).thenReturn(principal);
        when(principal.getAmount()).thenReturn(BigDecimal.valueOf(40));
        when(currency.getCode()).thenReturn("UGX");
        assertThatThrownBy(() -> service.requireDisbursement(context, loan, command, false))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(policy).allows(7L, MonetaryAuthorityType.DISBURSEMENT, "UGX", BigDecimal.valueOf(40));
    }

    @Test
    void preflightUsesArtificialTrancheAmountWithoutCreatingIt() {
        var command = mock(JsonCommand.class);
        var product = mock(LoanProduct.class);
        when(loan.loanProduct()).thenReturn(product);
        when(product.isDisallowExpectedDisbursements()).thenReturn(true);
        when(loan.getDisbursedAmount()).thenReturn(BigDecimal.valueOf(50));
        when(currency.getCode()).thenReturn("UGX");
        assertThatThrownBy(() -> service.requireDisbursement(context, loan, command, true))
                .isInstanceOf(GeneralPlatformDomainRuleException.class);
        verify(policy).allows(7L, MonetaryAuthorityType.DISBURSEMENT, "UGX", BigDecimal.valueOf(50));
        verify(loan, never()).getAllDisbursementDetails();
    }

}
