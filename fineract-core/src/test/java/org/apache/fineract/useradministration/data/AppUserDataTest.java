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
package org.apache.fineract.useradministration.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.junit.jupiter.api.Test;

class AppUserDataTest {

    @Test
    void shouldHoldSuspensionAndLastLoginAtState() {
        LocalDateTime loginTime = LocalDateTime.of(2026, 9, 27, 1, 0, 0);
        AppUserData user = AppUserData.instance(1L, "admin", "admin@nsimbi.org", 1L, "Head Office", "System", "Admin",
                Collections.emptyList(), Collections.emptyList(), null, false, true, loginTime);

        assertTrue(user.isSuspended());
        assertEquals(loginTime, user.getLastLoginAt());
        assertEquals("admin", user.username());
    }

    @Test
    void shouldDefaultSuspensionToFalseWhenUsingLegacyInstance() {
        AppUserData user = AppUserData.instance(1L, "admin", "admin@nsimbi.org", 1L, "Head Office", "System", "Admin",
                Collections.emptyList(), Collections.emptyList(), null, false);

        assertFalse(user.isSuspended());
        assertNull(user.getLastLoginAt());
    }

    @Test
    void shouldPreserveSuspensionInTemplate() {
        LocalDateTime loginTime = LocalDateTime.of(2026, 9, 27, 1, 0, 0);
        AppUserData user = AppUserData.instance(1L, "admin", "admin@nsimbi.org", 1L, "Head Office", "System", "Admin",
                Collections.emptyList(), Collections.emptyList(), null, false, true, loginTime);

        AppUserData templated = AppUserData.template(user, List.of(OfficeData.dropdown(1L, "Head Office", "Head Office")));

        assertTrue(templated.isSuspended());
        assertEquals(loginTime, templated.getLastLoginAt());
    }
}
