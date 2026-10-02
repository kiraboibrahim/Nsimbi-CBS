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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleOperatingHours;
import org.apache.fineract.useradministration.domain.RoleOperatingHoursRepository;
import org.apache.fineract.useradministration.exception.OperatingHoursRestrictionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleOperatingHoursValidatorTest {

    @Mock
    private RoleOperatingHoursRepository roleOperatingHoursRepository;

    @Mock
    private AppUser appUser;

    @InjectMocks
    private RoleOperatingHoursValidator validator;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil
                .setTenant(FineractPlatformTenant.builder().id(1L).tenantIdentifier("default").name("default").timezoneId("UTC").build());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldThrowIllegalArgumentWhenUserIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateUserOperatingHours(null));
        assertEquals("User cannot be null for operating hours validation", exception.getMessage());
    }

    @Test
    void shouldPassWhenUserHasNoRoles() {
        when(appUser.getRoles()).thenReturn(Collections.emptySet());
        assertDoesNotThrow(() -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldPassWhenRolesHaveNoOperatingHoursConfigured() {
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);
        when(role.isDisabled()).thenReturn(false);
        when(appUser.getRoles()).thenReturn(Set.of(role));
        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldPassWhenCurrentTimeIsWithinRoleOperatingHours() {
        ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        ZonedDateTime now = ZonedDateTime.now(tenantZone);
        int currentDayOfWeek = now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);
        when(role.isDisabled()).thenReturn(false);
        when(appUser.getRoles()).thenReturn(Set.of(role));

        RoleOperatingHours hours = mock(RoleOperatingHours.class);
        when(hours.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours.isClosed()).thenReturn(false);
        when(hours.getOpenTime()).thenReturn(currentTime.minusHours(1));
        when(hours.getCloseTime()).thenReturn(currentTime.plusHours(1));

        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(List.of(hours));

        assertDoesNotThrow(() -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldPassWhenCurrentTimeIsExactlyAtOpenBoundary() {
        ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        ZonedDateTime now = ZonedDateTime.now(tenantZone);
        int currentDayOfWeek = now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);
        when(role.isDisabled()).thenReturn(false);
        when(appUser.getRoles()).thenReturn(Set.of(role));

        RoleOperatingHours hours = mock(RoleOperatingHours.class);
        when(hours.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours.isClosed()).thenReturn(false);
        when(hours.getOpenTime()).thenReturn(currentTime);
        when(hours.getCloseTime()).thenReturn(currentTime.plusHours(1));

        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(List.of(hours));

        assertDoesNotThrow(() -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldThrowWhenCurrentTimeIsBeforeOpenTime() {
        ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        ZonedDateTime now = ZonedDateTime.now(tenantZone);
        int currentDayOfWeek = now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);
        when(role.isDisabled()).thenReturn(false);
        when(appUser.getRoles()).thenReturn(Set.of(role));

        RoleOperatingHours hours = mock(RoleOperatingHours.class);
        when(hours.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours.isClosed()).thenReturn(false);
        when(hours.getOpenTime()).thenReturn(currentTime.plusMinutes(10));
        when(hours.getCloseTime()).thenReturn(currentTime.plusHours(2));

        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(List.of(hours));

        assertThrows(OperatingHoursRestrictionException.class, () -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldThrowWhenCurrentTimeIsAfterCloseTime() {
        ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        ZonedDateTime now = ZonedDateTime.now(tenantZone);
        int currentDayOfWeek = now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);
        when(role.isDisabled()).thenReturn(false);
        when(appUser.getRoles()).thenReturn(Set.of(role));

        RoleOperatingHours hours = mock(RoleOperatingHours.class);
        when(hours.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours.isClosed()).thenReturn(false);
        when(hours.getOpenTime()).thenReturn(currentTime.minusHours(2));
        when(hours.getCloseTime()).thenReturn(currentTime.minusMinutes(10));

        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(List.of(hours));

        assertThrows(OperatingHoursRestrictionException.class, () -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldThrowWhenDayIsMarkedClosed() {
        ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        ZonedDateTime now = ZonedDateTime.now(tenantZone);
        int currentDayOfWeek = now.getDayOfWeek().getValue();

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);
        when(role.isDisabled()).thenReturn(false);
        when(appUser.getRoles()).thenReturn(Set.of(role));

        RoleOperatingHours hours = mock(RoleOperatingHours.class);
        when(hours.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours.isClosed()).thenReturn(true);

        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(List.of(hours));

        assertThrows(OperatingHoursRestrictionException.class, () -> validator.validateUserOperatingHours(appUser));
    }

    @Test
    void shouldApplyPermissiveUnionAcrossMultipleRoles() {
        ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();
        ZonedDateTime now = ZonedDateTime.now(tenantZone);
        int currentDayOfWeek = now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        Role roleClosed = mock(Role.class);
        when(roleClosed.getId()).thenReturn(1L);
        when(roleClosed.isDisabled()).thenReturn(false);

        Role roleOpen = mock(Role.class);
        when(roleOpen.getId()).thenReturn(2L);
        when(roleOpen.isDisabled()).thenReturn(false);

        Set<Role> roles = new java.util.LinkedHashSet<>();
        roles.add(roleClosed);
        roles.add(roleOpen);
        when(appUser.getRoles()).thenReturn(roles);

        // Role 1 is closed today
        RoleOperatingHours hours1 = mock(RoleOperatingHours.class);
        when(hours1.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours1.isClosed()).thenReturn(true);
        when(roleOperatingHoursRepository.findByRoleId(1L)).thenReturn(List.of(hours1));

        // Role 2 is open currently
        RoleOperatingHours hours2 = mock(RoleOperatingHours.class);
        when(hours2.getDayOfWeek()).thenReturn(currentDayOfWeek);
        when(hours2.isClosed()).thenReturn(false);
        when(hours2.getOpenTime()).thenReturn(currentTime.minusMinutes(30));
        when(hours2.getCloseTime()).thenReturn(currentTime.plusMinutes(30));
        when(roleOperatingHoursRepository.findByRoleId(2L)).thenReturn(List.of(hours2));

        // Permissive union: passes because role 2 window is open
        assertDoesNotThrow(() -> validator.validateUserOperatingHours(appUser));
    }
}
