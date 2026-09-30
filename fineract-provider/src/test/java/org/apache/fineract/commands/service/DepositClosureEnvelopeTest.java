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
package org.apache.fineract.commands.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.domain.SavingsTransactionOrigin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DepositClosureEnvelopeTest {

    @ParameterizedTest
    @CsvSource({ "true,false,FIXED_DEPOSIT_CLOSE", "true,true,FIXED_DEPOSIT_PREMATURE_CLOSE", "false,false,RECURRING_DEPOSIT_CLOSE",
            "false,true,RECURRING_DEPOSIT_PREMATURE_CLOSE" })
    void exactClosureRoutingAndReservedMetadataProtection(boolean fixed, boolean premature, SavingsTransactionKind kind) {
        var builder = new CommandWrapperBuilder().withJson("{}");
        var wrapper = (fixed ? (premature ? builder.prematureCloseFixedDepositAccount(1L) : builder.closeFixedDepositAccount(1L))
                : (premature ? builder.prematureCloseRecurringDepositAccount(1L) : builder.closeRecurringDepositAccount(1L))).build();
        assertThat(SavingsTransactionKind.fromCommand(wrapper.actionName(), wrapper.entityName())).isEqualTo(kind);
        assertThat(kind.accepts(SavingsTransactionOrigin.STAFF_API)).isTrue();
        assertThat(SavingsTransactionCommandEnvelope.encode("{}", kind, SavingsTransactionOrigin.STAFF_API)).contains(kind.name());
        assertThrows(RuntimeException.class,
                () -> SavingsTransactionCommandEnvelope.encode("{\"_serverCommand\":{}}", kind, SavingsTransactionOrigin.STAFF_API));
    }
}
