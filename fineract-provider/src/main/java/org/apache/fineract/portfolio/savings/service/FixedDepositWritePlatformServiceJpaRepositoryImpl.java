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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.savings.data.FixedDepositDataValidator;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.FixedDeposit;
import org.apache.fineract.portfolio.savings.domain.FixedDepositFundingSource;
import org.apache.fineract.portfolio.savings.domain.FixedDepositInterestInterval;
import org.apache.fineract.portfolio.savings.domain.FixedDepositPayoutOption;
import org.apache.fineract.portfolio.savings.domain.FixedDepositRepository;
import org.apache.fineract.portfolio.savings.domain.FixedDepositStatus;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixedDepositWritePlatformServiceJpaRepositoryImpl implements FixedDepositWritePlatformService {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final FixedDepositRepository fixedDepositRepository;
    private final FixedDepositDataValidator fixedDepositDataValidator;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;

    @Transactional
    @Override
    public CommandProcessingResult createFixedDeposit(final JsonCommand command) {
        this.fixedDepositDataValidator.validateForCreate(command.json());

        final AppUser currentUser = this.context.authenticatedUser();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());

        final Long customerId = this.fromApiJsonHelper.extractLongNamed("customerId", element) != null
                ? this.fromApiJsonHelper.extractLongNamed("customerId", element)
                : this.fromApiJsonHelper.extractLongNamed("clientId", element);
        final Client customer = this.clientRepositoryWrapper.findOneWithNotFoundDetection(customerId);

        final BigDecimal principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principalAmount", element) != null
                ? this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principalAmount", element)
                : this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("depositAmount", element);

        final Integer periodMonths = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("periodMonths", element) != null
                ? this.fromApiJsonHelper.extractIntegerWithLocaleNamed("periodMonths", element)
                : this.fromApiJsonHelper.extractIntegerWithLocaleNamed("depositPeriod", element);

        final BigDecimal interestRateAnnual = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateAnnual", element) != null
                ? this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateAnnual", element)
                : this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRate", element);

        final LocalDate startsOn = this.fromApiJsonHelper.extractLocalDateNamed("startsOn", element) != null
                ? this.fromApiJsonHelper.extractLocalDateNamed("startsOn", element)
                : this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        final String fundingSourceStr = this.fromApiJsonHelper.extractStringNamed("fundingSource", element);
        final FixedDepositFundingSource fundingSource = FixedDepositFundingSource.fromString(fundingSourceStr);

        final Long payoutSavingsAccountId = this.fromApiJsonHelper.extractLongNamed("payoutSavingsAccountId", element) != null
                ? this.fromApiJsonHelper.extractLongNamed("payoutSavingsAccountId", element)
                : this.fromApiJsonHelper.extractLongNamed("linkAccountId", element);
        final SavingsAccount payoutSavingsAccount = this.savingsAccountRepositoryWrapper
                .findOneWithNotFoundDetection(payoutSavingsAccountId);

        SavingsAccount fundingSavingsAccount = null;
        if (FixedDepositFundingSource.SAVINGS_OFFSET.equals(fundingSource)) {
            final Long fundingSavingsAccountId = this.fromApiJsonHelper.extractLongNamed("fundingSavingsAccountId", element) != null
                    ? this.fromApiJsonHelper.extractLongNamed("fundingSavingsAccountId", element)
                    : payoutSavingsAccountId;
            fundingSavingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(fundingSavingsAccountId);

            // Business rule: Place funds immediately on hold on source savings account
            fundingSavingsAccount.holdAmount(principalAmount);
            this.savingsAccountRepositoryWrapper.save(fundingSavingsAccount);
        }

        String depositorName = this.fromApiJsonHelper.extractStringNamed("depositorName", element);
        if (StringUtils.isBlank(depositorName)) {
            depositorName = customer.getDisplayName();
        }

        String depositorPhone = this.fromApiJsonHelper.extractStringNamed("depositorPhone", element);
        if (StringUtils.isBlank(depositorPhone)) {
            depositorPhone = customer.mobileNo() != null ? customer.mobileNo() : "";
        }

        String interestIntervalStr = this.fromApiJsonHelper.extractStringNamed("interestInterval", element);
        FixedDepositInterestInterval interestInterval = FixedDepositInterestInterval.fromString(interestIntervalStr);
        if (interestInterval == null) {
            interestInterval = FixedDepositInterestInterval.MONTHLY;
        }

        String payoutOptionStr = this.fromApiJsonHelper.extractStringNamed("payoutOption", element);
        FixedDepositPayoutOption payoutOption = FixedDepositPayoutOption.fromString(payoutOptionStr);
        if (payoutOption == null) {
            payoutOption = FixedDepositPayoutOption.PRINCIPAL_AND_INTEREST;
        }

        String depositNumber = this.fromApiJsonHelper.extractStringNamed("depositNumber", element);
        if (StringUtils.isBlank(depositNumber)) {
            depositNumber = generateDepositNumber(customer);
        }

        final FixedDeposit fixedDeposit = FixedDeposit.create(depositNumber, customer, fundingSource, fundingSavingsAccount,
                payoutSavingsAccount, depositorName, depositorPhone, principalAmount, startsOn, periodMonths, interestRateAnnual,
                interestInterval, payoutOption, currentUser);

        this.fixedDepositRepository.save(fixedDeposit);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(fixedDeposit.getId()) //
                .withClientId(customer.getId()) //
                .withOfficeId(customer.officeId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveFixedDeposit(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final FixedDeposit fixedDeposit = this.fixedDepositRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.found",
                        "Fixed deposit not found with id " + id));

        if (!fixedDeposit.isPending()) {
            throw new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.pending",
                    "Fixed deposit cannot be approved in current status: " + fixedDeposit.getStatus().name());
        }

        // Segregation of duties: Maker cannot approve their own application
        if (fixedDeposit.getCreatedBy() != null && fixedDeposit.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.maker.cannot.approve",
                    "Maker of the fixed deposit placement cannot approve it");
        }

        // If offset funding, release the hold and record withdrawal transaction from source savings
        if (FixedDepositFundingSource.SAVINGS_OFFSET.equals(fixedDeposit.getFundingSource())
                && fixedDeposit.getFundingSavingsAccount() != null) {
            final SavingsAccount fundingAccount = fixedDeposit.getFundingSavingsAccount();
            fundingAccount.releaseOnHoldAmount(fixedDeposit.getPrincipalAmount());
            this.savingsAccountRepositoryWrapper.save(fundingAccount);

            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            final boolean isAccountTransfer = true;
            final boolean isRegularTransaction = true;
            final boolean isApplyWithdrawFee = false;
            final boolean isInterestTransfer = false;
            final boolean isWithdrawBalance = false;
            final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                    isRegularTransaction, isApplyWithdrawFee, isInterestTransfer, isWithdrawBalance);
            final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fundingAccount, fmt,
                    LocalDate.now(), fixedDeposit.getPrincipalAmount(), null, transactionBooleanValues, false);
            this.savingsAccountTransactionRepository.save(withdrawal);
        }

        fixedDeposit.approve(currentUser);
        this.fixedDepositRepository.save(fixedDeposit);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(fixedDeposit.getId()) //
                .withClientId(fixedDeposit.getCustomer().getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectFixedDeposit(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final FixedDeposit fixedDeposit = this.fixedDepositRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.found",
                        "Fixed deposit not found with id " + id));

        if (!fixedDeposit.isPending()) {
            throw new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.pending",
                    "Fixed deposit cannot be rejected in current status: " + fixedDeposit.getStatus().name());
        }

        final String reason = command.stringValueOfParameterNamed("rejectionReason");

        // If offset funding was selected, release the placed hold
        if (FixedDepositFundingSource.SAVINGS_OFFSET.equals(fixedDeposit.getFundingSource())
                && fixedDeposit.getFundingSavingsAccount() != null) {
            final SavingsAccount fundingAccount = fixedDeposit.getFundingSavingsAccount();
            fundingAccount.releaseOnHoldAmount(fixedDeposit.getPrincipalAmount());
            this.savingsAccountRepositoryWrapper.save(fundingAccount);
        }

        fixedDeposit.reject(currentUser, reason);
        this.fixedDepositRepository.save(fixedDeposit);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(fixedDeposit.getId()) //
                .withClientId(fixedDeposit.getCustomer().getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult terminateFixedDeposit(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final FixedDeposit fixedDeposit = this.fixedDepositRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.found",
                        "Fixed deposit not found with id " + id));

        if (!fixedDeposit.isRunning() && !FixedDepositStatus.MATURED_NOT_AWARDED.equals(fixedDeposit.getStatus())) {
            throw new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.running",
                    "Fixed deposit cannot be terminated in status: " + fixedDeposit.getStatus().name());
        }

        final String interestDecision = command.stringValueOfParameterNamed("interestDecision");
        final boolean withInterest = "WITH_INTEREST".equalsIgnoreCase(interestDecision);

        final LocalDate terminationDate = command.localDateValueOfParameterNamed("terminationDate") != null
                ? command.localDateValueOfParameterNamed("terminationDate")
                : LocalDate.now();

        BigDecimal accruedInterest = BigDecimal.ZERO;
        FixedDepositStatus terminalStatus = FixedDepositStatus.TERMINATED_NO_INTEREST;

        if (withInterest) {
            terminalStatus = FixedDepositStatus.TERMINATED_WITH_INTEREST;
            final long daysElapsed = ChronoUnit.DAYS.between(fixedDeposit.getStartsOn(), terminationDate);
            if (daysElapsed > 0) {
                // Annual simple interest formula: Principal * (Rate / 100) * (Days / 365)
                final BigDecimal annualRate = fixedDeposit.getInterestRateAnnual().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                final BigDecimal yearFraction = BigDecimal.valueOf(daysElapsed).divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
                accruedInterest = fixedDeposit.getPrincipalAmount().multiply(annualRate).multiply(yearFraction).setScale(2, RoundingMode.HALF_UP);
            }
        }

        final BigDecimal payoutAmount = fixedDeposit.getPrincipalAmount().add(accruedInterest);

        // Credit payout to destination savings account
        final SavingsAccount payoutAccount = fixedDeposit.getPayoutSavingsAccount();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final boolean isAccountTransfer = true;
        final boolean isRegularTransaction = true;
        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(payoutAccount, fmt, terminationDate,
                payoutAmount, null, isAccountTransfer, isRegularTransaction, false);
        this.savingsAccountTransactionRepository.save(deposit);

        fixedDeposit.terminate(terminalStatus, accruedInterest, currentUser);
        this.fixedDepositRepository.save(fixedDeposit);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(fixedDeposit.getId()) //
                .withClientId(fixedDeposit.getCustomer().getId()) //
                .build();
    }

    private String generateDepositNumber(final Client client) {
        final Long branchId = client.getOffice() != null ? client.getOffice().getId() : 1L;
        final long count = this.fixedDepositRepository.count() + 1;
        return String.format("%04dFD%06d", branchId, count);
    }
}
