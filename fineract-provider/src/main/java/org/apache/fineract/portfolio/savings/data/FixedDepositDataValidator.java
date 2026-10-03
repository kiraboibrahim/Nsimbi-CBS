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
package org.apache.fineract.portfolio.savings.data;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.FixedDepositFundingSource;
import org.apache.fineract.portfolio.savings.domain.FixedDepositInterestInterval;
import org.apache.fineract.portfolio.savings.domain.FixedDepositPayoutOption;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FixedDepositDataValidator {

    public static final BigDecimal MIN_OPERATING_RESERVE = BigDecimal.valueOf(5000);

    private final FromJsonHelper fromApiJsonHelper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new PlatformApiDataValidationException("error.msg.null", "JSON body cannot be null", new ArrayList<>());
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("fixeddeposit");

        final Long customerId = this.fromApiJsonHelper.extractLongNamed("customerId", element) != null
                ? this.fromApiJsonHelper.extractLongNamed("customerId", element)
                : this.fromApiJsonHelper.extractLongNamed("clientId", element);
        baseDataValidator.reset().parameter("customerId").value(customerId).notNull();

        final BigDecimal principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principalAmount", element) != null
                ? this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principalAmount", element)
                : this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("depositAmount", element);
        baseDataValidator.reset().parameter("principalAmount").value(principalAmount).notNull().positiveAmount();

        final Integer periodMonths = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("periodMonths", element) != null
                ? this.fromApiJsonHelper.extractIntegerWithLocaleNamed("periodMonths", element)
                : this.fromApiJsonHelper.extractIntegerWithLocaleNamed("depositPeriod", element);
        baseDataValidator.reset().parameter("periodMonths").value(periodMonths).notNull().integerGreaterThanZero();

        final BigDecimal interestRateAnnual = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateAnnual", element) != null
                ? this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRateAnnual", element)
                : this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRate", element);
        baseDataValidator.reset().parameter("interestRateAnnual").value(interestRateAnnual).notNull().zeroOrPositiveAmount();

        final LocalDate startsOn = this.fromApiJsonHelper.extractLocalDateNamed("startsOn", element) != null
                ? this.fromApiJsonHelper.extractLocalDateNamed("startsOn", element)
                : this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);
        baseDataValidator.reset().parameter("startsOn").value(startsOn).notNull();

        final String fundingSourceStr = this.fromApiJsonHelper.extractStringNamed("fundingSource", element);
        baseDataValidator.reset().parameter("fundingSource").value(fundingSourceStr).notBlank();

        final FixedDepositFundingSource fundingSource = FixedDepositFundingSource.fromString(fundingSourceStr);
        if (fundingSource == null) {
            baseDataValidator.reset().parameter("fundingSource").failWithCode("invalid.funding.source",
                    "Funding source must be TILL_CASH or SAVINGS_OFFSET");
        }

        final Long payoutSavingsAccountId = this.fromApiJsonHelper.extractLongNamed("payoutSavingsAccountId", element) != null
                ? this.fromApiJsonHelper.extractLongNamed("payoutSavingsAccountId", element)
                : this.fromApiJsonHelper.extractLongNamed("linkAccountId", element);
        baseDataValidator.reset().parameter("payoutSavingsAccountId").value(payoutSavingsAccountId).notNull();

        final String interestIntervalStr = this.fromApiJsonHelper.extractStringNamed("interestInterval", element);
        if (StringUtils.isNotBlank(interestIntervalStr) && FixedDepositInterestInterval.fromString(interestIntervalStr) == null) {
            baseDataValidator.reset().parameter("interestInterval").failWithCode("invalid.interest.interval",
                    "Interest interval must be monthly, yearly, or Not applicable");
        }

        final String payoutOptionStr = this.fromApiJsonHelper.extractStringNamed("payoutOption", element);
        if (StringUtils.isNotBlank(payoutOptionStr) && FixedDepositPayoutOption.fromString(payoutOptionStr) == null) {
            baseDataValidator.reset().parameter("payoutOption").failWithCode("invalid.payout.option",
                    "Payout option must be Principal or Principal & Interest");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        // Entity level validations
        final Client customer = this.clientRepositoryWrapper.findOneWithNotFoundDetection(customerId);
        if (customer.isNotActive()) {
            baseDataValidator.reset().parameter("customerId").failWithCode("client.must.be.active", "Target customer must be active");
        }

        final SavingsAccount payoutAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(payoutSavingsAccountId);
        if (payoutAccount.isNotActive()) {
            baseDataValidator.reset().parameter("payoutSavingsAccountId").failWithCode("payout.account.must.be.active",
                    "Payout savings account must be in active status");
        }
        if (!payoutAccount.clientId().equals(customerId)) {
            baseDataValidator.reset().parameter("payoutSavingsAccountId").failWithCode("payout.account.belongs.to.different.client",
                    "Payout savings account must belong to the target member");
        }

        if (FixedDepositFundingSource.SAVINGS_OFFSET.equals(fundingSource)) {
            final Long fundingSavingsAccountId = this.fromApiJsonHelper.extractLongNamed("fundingSavingsAccountId", element) != null
                    ? this.fromApiJsonHelper.extractLongNamed("fundingSavingsAccountId", element)
                    : payoutSavingsAccountId;

            if (fundingSavingsAccountId == null) {
                baseDataValidator.reset().parameter("fundingSavingsAccountId").failWithCode("funding.savings.account.mandatory.for.offset",
                        "Funding savings account is mandatory when offset funding source is selected");
            } else {
                final SavingsAccount fundingAccount = this.savingsAccountRepositoryWrapper
                        .findOneWithNotFoundDetection(fundingSavingsAccountId);
                if (fundingAccount.isNotActive()) {
                    baseDataValidator.reset().parameter("fundingSavingsAccountId").failWithCode("funding.account.must.be.active",
                            "Funding savings account must be in active status");
                }
                if (!fundingAccount.clientId().equals(customerId)) {
                    baseDataValidator.reset().parameter("fundingSavingsAccountId").failWithCode(
                            "funding.account.belongs.to.different.client",
                            "Funding savings account must belong to the target member");
                }

                // Balance check: Available balance = actualBalance - 5000 reserve - onHoldFunds
                final BigDecimal availableBalance = calculateAvailableBalance(fundingAccount);
                if (principalAmount != null && availableBalance.compareTo(principalAmount) < 0) {
                    baseDataValidator.reset().parameter("principalAmount").failWithCode(
                            "insufficient.available.balance",
                            "Insufficient available balance in source savings account");
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public BigDecimal calculateAvailableBalance(final SavingsAccount account) {
        BigDecimal balance = account.getAccountBalance() != null ? account.getAccountBalance() : BigDecimal.ZERO;
        final BigDecimal minRequired = account.getMinRequiredBalance() != null && account.getMinRequiredBalance().compareTo(BigDecimal.ZERO) > 0
                ? account.getMinRequiredBalance()
                : MIN_OPERATING_RESERVE;
        balance = balance.subtract(minRequired);

        if (account.getSavingsHoldAmount() != null) {
            balance = balance.subtract(account.getSavingsHoldAmount());
        }
        return balance.max(BigDecimal.ZERO);
    }
}
