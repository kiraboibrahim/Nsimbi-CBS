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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DebitCardIssuanceDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    public void validateForRequest(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new PlatformApiDataValidationException("error.msg.null", "JSON body cannot be null", new ArrayList<>());
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("debitcard");

        final Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed("savingsAccountId", element);
        baseDataValidator.reset().parameter("savingsAccountId").value(savingsAccountId).notNull();

        final String cardPanMasked = this.fromApiJsonHelper.extractStringNamed("cardPanMasked", element);
        baseDataValidator.reset().parameter("cardPanMasked").value(cardPanMasked).notBlank().notExceedingLengthOf(30);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        final SavingsAccount account = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
        if (account.isNotActive()) {
            baseDataValidator.reset().parameter("savingsAccountId").failWithCode("savings.account.must.be.active",
                    "Linked savings account must be in active status");
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
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("debitcard");

        if ("reject".equalsIgnoreCase(action)) {
            final String reason = this.fromApiJsonHelper.extractStringNamed("rejectionReason", element);
            baseDataValidator.reset().parameter("rejectionReason").value(reason).notBlank().notExceedingLengthOf(500);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
