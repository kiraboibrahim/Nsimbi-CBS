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
package org.apache.fineract.portfolio.savings.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.junit.jupiter.api.Test;

class SavingsAccountBlockTest {

    @Test
    void block_shouldSucceedWhenActiveAndNotBlocked() {
        SavingsAccount account = new SavingsAccount() {};
        account.status = SavingsAccountStatusType.ACTIVE.getValue();

        Map<String, Object> changes = account.block();

        assertThat(changes).isNotNull();
        assertThat(changes).containsKey(SavingsApiConstants.subStatusParamName);
        assertThat(account.getSubStatus()).isEqualTo(SavingsAccountSubStatusEnum.BLOCK.getValue());
    }

    @Test
    void block_shouldThrowExceptionWhenAlreadyBlocked() {
        SavingsAccount account = new SavingsAccount() {};
        account.status = SavingsAccountStatusType.ACTIVE.getValue();
        account.sub_status = SavingsAccountSubStatusEnum.BLOCK.getValue();

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class, account::block);
        assertThat(ex.getErrors()).hasSize(1);
        assertThat(ex.getErrors().get(0).getUserMessageGlobalisationCode()).contains("savings.account.already.blocked");
    }

    @Test
    void block_shouldThrowExceptionWhenNotActive() {
        SavingsAccount account = new SavingsAccount() {};
        account.status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue();

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class, account::block);
        assertThat(ex.getErrors()).hasSize(1);
        assertThat(ex.getErrors().get(0).getUserMessageGlobalisationCode())
                .contains(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);
    }
}
