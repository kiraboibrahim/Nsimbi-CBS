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
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleOperatingHours;
import org.apache.fineract.useradministration.domain.RoleOperatingHoursRepository;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleOperatingHoursWritePlatformServiceImpl implements RoleOperatingHoursWritePlatformService {

    private final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final RoleOperatingHoursRepository roleOperatingHoursRepository;

    @Override
    @Transactional
    public CommandProcessingResult updateRoleOperatingHours(final Long roleId, final JsonCommand command) {
        this.context.authenticatedUser();
        final Role role = this.roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));

        validateForUpdate(command);

        final JsonArray scheduleArray = command.arrayOfParameterNamed("schedule");
        this.roleOperatingHoursRepository.deleteByRoleId(roleId);
        this.roleOperatingHoursRepository.flush();

        final List<RoleOperatingHours> newHours = new ArrayList<>();
        if (scheduleArray != null) {
            for (int i = 0; i < scheduleArray.size(); i++) {
                final JsonObject dayObj = scheduleArray.get(i).getAsJsonObject();
                final int dayOfWeek = dayObj.get("dayOfWeek").getAsInt();
                final boolean isClosed = dayObj.has("isClosed") && dayObj.get("isClosed").getAsBoolean();

                LocalTime openTime = null;
                LocalTime closeTime = null;
                if (!isClosed) {
                    if (dayObj.has("openTime") && !dayObj.get("openTime").isJsonNull()) {
                        openTime = LocalTime.parse(dayObj.get("openTime").getAsString());
                    }
                    if (dayObj.has("closeTime") && !dayObj.get("closeTime").isJsonNull()) {
                        closeTime = LocalTime.parse(dayObj.get("closeTime").getAsString());
                    }
                }

                newHours.add(new RoleOperatingHours(role, dayOfWeek, openTime, closeTime, isClosed));
            }
            this.roleOperatingHoursRepository.saveAll(newHours);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(roleId).build();
    }

    private void validateForUpdate(final JsonCommand command) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("roleOperatingHours");

        final JsonElement element = command.parsedJson();
        baseDataValidator.reset().parameter("schedule").value(element).notNull();

        if (command.parameterExists("schedule")) {
            final JsonArray scheduleArray = command.arrayOfParameterNamed("schedule");
            if (scheduleArray == null || scheduleArray.isEmpty()) {
                baseDataValidator.reset().parameter("schedule").failWithCode("cannot.be.empty");
            } else {
                for (int i = 0; i < scheduleArray.size(); i++) {
                    final JsonObject dayObj = scheduleArray.get(i).getAsJsonObject();
                    final Integer dayOfWeek = dayObj.has("dayOfWeek") && !dayObj.get("dayOfWeek").isJsonNull()
                            ? dayObj.get("dayOfWeek").getAsInt()
                            : null;
                    baseDataValidator.reset().parameter("schedule[" + i + "].dayOfWeek").value(dayOfWeek).notNull().inMinMaxRange(1, 7);

                    final boolean isClosed = dayObj.has("isClosed") && dayObj.get("isClosed").getAsBoolean();
                    if (!isClosed) {
                        final String openTimeStr = dayObj.has("openTime") && !dayObj.get("openTime").isJsonNull()
                                ? dayObj.get("openTime").getAsString()
                                : null;
                        final String closeTimeStr = dayObj.has("closeTime") && !dayObj.get("closeTime").isJsonNull()
                                ? dayObj.get("closeTime").getAsString()
                                : null;

                        baseDataValidator.reset().parameter("schedule[" + i + "].openTime").value(openTimeStr).notBlank();
                        baseDataValidator.reset().parameter("schedule[" + i + "].closeTime").value(closeTimeStr).notBlank();

                        LocalTime open = null;
                        LocalTime close = null;
                        if (openTimeStr != null) {
                            try {
                                open = LocalTime.parse(openTimeStr);
                            } catch (DateTimeParseException e) {
                                baseDataValidator.reset().parameter("schedule[" + i + "].openTime").failWithCode("invalid.time.format");
                            }
                        }
                        if (closeTimeStr != null) {
                            try {
                                close = LocalTime.parse(closeTimeStr);
                            } catch (DateTimeParseException e) {
                                baseDataValidator.reset().parameter("schedule[" + i + "].closeTime").failWithCode("invalid.time.format");
                            }
                        }
                        if (open != null && close != null && !open.isBefore(close)) {
                            baseDataValidator.reset().parameter("schedule[" + i + "].openTime").failWithCode("open.must.be.before.close",
                                    "Open time must be before close time");
                        }
                    }
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
