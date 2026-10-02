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
package org.apache.fineract.useradministration.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.TransactionLimitType;
import org.apache.fineract.useradministration.domain.UserTransactionLimit;
import org.apache.fineract.useradministration.domain.UserTransactionLimitRepository;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserTransactionLimitWritePlatformServiceImpl implements UserTransactionLimitWritePlatformService {

    private final PlatformSecurityContext context;
    private final AppUserRepository appUserRepository;
    private final UserTransactionLimitRepository userTransactionLimitRepository;

    @Override
    @Transactional
    public CommandProcessingResult updateUserTransactionLimits(final Long userId, final JsonCommand command) {
        this.context.authenticatedUser();
        final AppUser user = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        validateForUpdate(command);

        final JsonArray limitsArray = command.arrayOfParameterNamed("limits");
        this.userTransactionLimitRepository.deleteByAppUserId(userId);
        this.userTransactionLimitRepository.flush();

        final List<UserTransactionLimit> newLimits = new ArrayList<>();
        if (limitsArray != null) {
            for (int i = 0; i < limitsArray.size(); i++) {
                final JsonObject limitObj = limitsArray.get(i).getAsJsonObject();
                final String limitTypeStr = limitObj.get("limitType").getAsString();
                final TransactionLimitType limitType = TransactionLimitType.fromString(limitTypeStr);
                final BigDecimal minAmount = limitObj.get("minAmount").getAsBigDecimal();
                final BigDecimal maxAmount = limitObj.get("maxAmount").getAsBigDecimal();

                newLimits.add(new UserTransactionLimit(user, limitType, minAmount, maxAmount));
            }
            this.userTransactionLimitRepository.saveAll(newLimits);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(userId).build();
    }

    private void validateForUpdate(final JsonCommand command) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("userTransactionLimits");

        final JsonElement element = command.parsedJson();
        baseDataValidator.reset().parameter("limits").value(element).notNull();

        if (command.parameterExists("limits")) {
            final JsonArray limitsArray = command.arrayOfParameterNamed("limits");
            if (limitsArray == null || limitsArray.isEmpty()) {
                baseDataValidator.reset().parameter("limits").failWithCode("cannot.be.empty");
            } else {
                for (int i = 0; i < limitsArray.size(); i++) {
                    final JsonObject limitObj = limitsArray.get(i).getAsJsonObject();
                    final String limitTypeStr = limitObj.has("limitType") && !limitObj.get("limitType").isJsonNull()
                            ? limitObj.get("limitType").getAsString()
                            : null;
                    baseDataValidator.reset().parameter("limits[" + i + "].limitType").value(limitTypeStr).notBlank();
                    if (limitTypeStr != null) {
                        try {
                            TransactionLimitType.fromString(limitTypeStr);
                        } catch (IllegalArgumentException e) {
                            baseDataValidator.reset().parameter("limits[" + i + "].limitType").failWithCode("invalid.limit.type");
                        }
                    }

                    final BigDecimal minAmount = limitObj.has("minAmount") && !limitObj.get("minAmount").isJsonNull()
                            ? limitObj.get("minAmount").getAsBigDecimal()
                            : null;
                    final BigDecimal maxAmount = limitObj.has("maxAmount") && !limitObj.get("maxAmount").isJsonNull()
                            ? limitObj.get("maxAmount").getAsBigDecimal()
                            : null;

                    baseDataValidator.reset().parameter("limits[" + i + "].minAmount").value(minAmount).notNull().zeroOrPositiveAmount();
                    baseDataValidator.reset().parameter("limits[" + i + "].maxAmount").value(maxAmount).notNull().zeroOrPositiveAmount();

                    if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
                        baseDataValidator.reset().parameter("limits[" + i + "].minAmount").failWithCode("min.cannot.exceed.max",
                                "Minimum limit cannot exceed maximum limit");
                    }
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
