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

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleOperatingHours;
import org.apache.fineract.useradministration.domain.RoleOperatingHoursRepository;
import org.apache.fineract.useradministration.exception.OperatingHoursRestrictionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleOperatingHoursValidator {

    private final RoleOperatingHoursRepository roleOperatingHoursRepository;

    public void validateUserOperatingHours(final AppUser user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for operating hours validation");
        }

        final Set<Role> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return;
        }

        final ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        final ZonedDateTime now = ZonedDateTime.now(tenantZone);
        final int currentDayOfWeek = now.getDayOfWeek().getValue();
        final LocalTime currentTime = now.toLocalTime();

        final List<RoleOperatingHours> applicableHours = new ArrayList<>();
        boolean hasConfiguredHours = false;

        for (final Role role : roles) {
            if (!Boolean.TRUE.equals(role.isDisabled())) {
                final List<RoleOperatingHours> roleHours = this.roleOperatingHoursRepository.findByRoleId(role.getId());
                if (!roleHours.isEmpty()) {
                    hasConfiguredHours = true;
                    for (final RoleOperatingHours hour : roleHours) {
                        if (hour.getDayOfWeek() == currentDayOfWeek) {
                            applicableHours.add(hour);
                        }
                    }
                }
            }
        }

        // If no active roles have any operating hours configured, access is unrestricted
        if (!hasConfiguredHours) {
            return;
        }

        // Permissive union: access is granted if AT LEAST ONE role window is currently open
        for (final RoleOperatingHours hour : applicableHours) {
            if (!hour.isClosed() && hour.getOpenTime() != null && hour.getCloseTime() != null) {
                // inclusive boundaries: openTime <= currentTime <= closeTime
                if (!currentTime.isBefore(hour.getOpenTime()) && !currentTime.isAfter(hour.getCloseTime())) {
                    return;
                }
            }
        }

        throw new OperatingHoursRestrictionException("Access is restricted outside permitted operating hours for current role schedule.");
    }
}
