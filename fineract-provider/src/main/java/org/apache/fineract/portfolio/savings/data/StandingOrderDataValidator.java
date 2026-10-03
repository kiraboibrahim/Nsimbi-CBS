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
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.StandingOrderFrequency;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StandingOrderDataValidator {

    public static final BigDecimal MIN_OPERATING_RESERVE = BigDecimal.valueOf(5000);

    private final FromJsonHelper fromApiJsonHelper;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new PlatformApiDataValidationException("error.msg.null", "JSON body cannot be null", new ArrayList<>());
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("standingorder");

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final Long sourceAccountId = this.fromApiJsonHelper.extractLongNamed("sourceAccountId", element);
        baseDataValidator.reset().parameter("sourceAccountId").value(sourceAccountId).notNull();

        final Long destinationAccountId = this.fromApiJsonHelper.extractLongNamed("destinationAccountId", element);
        baseDataValidator.reset().parameter("destinationAccountId").value(destinationAccountId).notNull();

        if (sourceAccountId != null && destinationAccountId != null && sourceAccountId.equals(destinationAccountId)) {
            baseDataValidator.reset().parameter("destinationAccountId").failWithCode("source.and.destination.cannot.be.same",
                    "Source and destination savings accounts cannot be the same");
        }

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        final String frequencyStr = this.fromApiJsonHelper.extractStringNamed("frequency", element);
        baseDataValidator.reset().parameter("frequency").value(frequencyStr).notBlank();

        final StandingOrderFrequency frequency = StandingOrderFrequency.fromString(frequencyStr);
        if (frequency == null) {
            baseDataValidator.reset().parameter("frequency").failWithCode("invalid.frequency",
                    "Frequency must be DAILY, WEEKLY, or MONTHLY");
        }

        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed("startDate", element);
        baseDataValidator.reset().parameter("startDate").value(startDate).notNull();

        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed("endDate", element);
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            baseDataValidator.reset().parameter("endDate").failWithCode("end.date.cannot.be.before.start.date",
                    "End date cannot be earlier than start date");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        // Validate source and destination account statuses
        final SavingsAccount sourceAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(sourceAccountId);
        if (sourceAccount.isNotActive()) {
            baseDataValidator.reset().parameter("sourceAccountId").failWithCode("source.account.must.be.active",
                    "Source savings account must be in active status");
        }

        final SavingsAccount destinationAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(destinationAccountId);
        if (destinationAccount.isNotActive()) {
            baseDataValidator.reset().parameter("destinationAccountId").failWithCode("destination.account.must.be.active",
                    "Destination savings account must be in active status");
        }

        // Available balance check on source account: actual balance - 5000 reserve - on-hold funds
        final BigDecimal availableBalance = calculateAvailableBalance(sourceAccount);
        if (amount != null && availableBalance.compareTo(amount) < 0) {
            baseDataValidator.reset().parameter("amount").failWithCode("insufficient.available.balance",
                    "Insufficient available balance in source savings account");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateForAction(final String json, final String action) {
        if (StringUtils.isBlank(json)) {
            return;
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("standingorder");

        if ("reject".equalsIgnoreCase(action)) {
            final String reason = this.fromApiJsonHelper.extractStringNamed("rejectionReason", element);
            baseDataValidator.reset().parameter("rejectionReason").value(reason).notBlank().notExceedingLengthOf(500);
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
