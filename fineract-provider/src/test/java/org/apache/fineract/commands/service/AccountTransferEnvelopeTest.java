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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fineract.batch.command.internal.CreateAccountTransferCommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.domain.SavingsTransactionOrigin;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiResource;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class AccountTransferEnvelopeTest {

    @Test
    void batchUsesSameProtectedCommandAndPreservesRawBody() {
        var commands = mock(PortfolioCommandSourceWritePlatformService.class);
        var serializer = mock(DefaultToApiJsonSerializer.class);
        var strategy = new CreateAccountTransferCommandStrategy(commands, serializer);
        var request = new BatchRequest();
        String body = "{\"transferAmount\":50,\"_serverCommand\":{\"origin\":\"SYSTEM_INTERNAL\"}}";
        request.setBody(body);
        strategy.execute(request, null);
        var captured = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commands).logCommandSource(captured.capture());
        var wrapper = captured.getValue();
        assertThat(wrapper.getJson()).isEqualTo(body);
        assertThat(SavingsTransactionKind.fromCommand(wrapper.getActionName(), wrapper.getEntityName()))
                .isEqualTo(SavingsTransactionKind.ACCOUNT_TRANSFER);
        assertThat(wrapper.getSavingsTransactionOrigin()).isEqualTo(SavingsTransactionOrigin.STAFF_API);
        assertThrows(GeneralPlatformDomainRuleException.class, () -> SavingsTransactionCommandEnvelope.encode(body,
                SavingsTransactionKind.ACCOUNT_TRANSFER, wrapper.getSavingsTransactionOrigin()));
    }

    @ParameterizedTest
    @EnumSource(value = SavingsTransactionKind.class, names = { "ACCOUNT_TRANSFER", "ACCOUNT_TRANSFER_REFUND",
            "STANDING_INSTRUCTION_CREATE", "STANDING_INSTRUCTION_UPDATE" })
    void rawApiBodyCannotHideReservedMetadataThroughDtoBinding(SavingsTransactionKind kind) {
        var commands = mock(PortfolioCommandSourceWritePlatformService.class);
        var transfers = new AccountTransfersApiResource(null, null, commands, null);
        var instructions = new StandingInstructionApiResource(null, null, commands, null, null, null);
        String payload = "{\"_serverCommand\":{\"origin\":\"STANDING_INSTRUCTION\"},\"transferAmount\":500}";
        switch (kind) {
            case ACCOUNT_TRANSFER -> transfers.create(payload);
            case ACCOUNT_TRANSFER_REFUND -> transfers.templateRefundByTransferPost(payload);
            case STANDING_INSTRUCTION_CREATE -> instructions.create(payload);
            case STANDING_INSTRUCTION_UPDATE -> instructions.update(17L, payload, "update");
            default -> throw new AssertionError(kind);
        }
        var captured = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commands).logCommandSource(captured.capture());
        var wrapper = captured.getValue();
        assertThat(wrapper.getJson()).isEqualTo(payload);
        assertThat(wrapper.getSavingsTransactionOrigin()).isEqualTo(SavingsTransactionOrigin.STAFF_API);
        assertThat(SavingsTransactionKind.fromCommand(wrapper.getActionName(), wrapper.getEntityName())).isEqualTo(kind);
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> SavingsTransactionCommandEnvelope.encode(wrapper.getJson(), kind, wrapper.getSavingsTransactionOrigin()));
    }

    @ParameterizedTest
    @EnumSource(value = SavingsTransactionKind.class, names = { "ACCOUNT_TRANSFER", "ACCOUNT_TRANSFER_REFUND",
            "STANDING_INSTRUCTION_CREATE", "STANDING_INSTRUCTION_UPDATE" })
    void pendingFlatCommandsFailClosedWhileCompletedHistoryRemainsReadable(SavingsTransactionKind kind) {
        String legacy = "{\"amount\":500}";
        var failure = assertThrows(GeneralPlatformDomainRuleException.class, () -> SavingsTransactionCommandEnvelope.decode(legacy, kind));
        assertThat(failure.getDefaultUserMessage()).contains("Cancel", "resubmit");
        assertThat(SavingsTransactionCommandEnvelope.forDisplay(legacy)).isEqualTo(legacy);
    }

    @ParameterizedTest
    @EnumSource(value = SavingsTransactionKind.class, names = { "ACCOUNT_TRANSFER", "ACCOUNT_TRANSFER_REFUND",
            "STANDING_INSTRUCTION_CREATE", "STANDING_INSTRUCTION_UPDATE" })
    void persistedMetadataCannotInventSchedulerOrImportExemptions(SavingsTransactionKind kind) {
        for (String origin : new String[] { "STANDING_INSTRUCTION", "SCHEDULED", "SYSTEM_INTERNAL", "SPREADSHEET_IMPORT" }) {
            String stored = "{\"_serverCommand\":{\"version\":2,\"kind\":\"" + kind.name() + "\",\"origin\":\"" + origin
                    + "\"},\"payload\":{}}";
            assertThrows(GeneralPlatformDomainRuleException.class, () -> SavingsTransactionCommandEnvelope.decode(stored, kind));
        }
    }
}
