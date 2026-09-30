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
package org.apache.fineract.portfolio.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.data.AccountTransfersDetailDataValidator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDataValidator;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class StandingInstructionDuesPolicyTest {

    private final FromJsonHelper json = new FromJsonHelper();

    @Test
    void newDuesRejectedWithActionableErrorBeforeOtherValidation() {
        var validator = new StandingInstructionDataValidator(json, new AccountTransfersDetailDataValidator(json));
        var error = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(command("{\"instructionType\":2}")));
        assertThat(error.getErrors()).anySatisfy(e -> assertThat(e.getDefaultUserMessage()).contains("enforceable maximum", "resubmit"));
    }

    @Test
    void creationCannotPersistOrInvokeAssembler() {
        var validator = new StandingInstructionDataValidator(json, new AccountTransfersDetailDataValidator(json));
        var assembler = mock(StandingInstructionAssembler.class);
        var details = mock(AccountTransferDetailRepository.class);
        var instructions = mock(StandingInstructionRepository.class);
        var writer = new org.apache.fineract.portfolio.account.service.StandingInstructionWritePlatformServiceImpl(validator, assembler,
                details, instructions);
        assertThrows(PlatformApiDataValidationException.class, () -> writer.create(command("{\"instructionType\":2}")));
        verifyNoInteractions(assembler, details, instructions);
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"status\":1}", "{\"amount\":100}", "{\"instructionType\":1}", "{\"recurrenceInterval\":2}",
            "{\"priority\":2}", "{\"status\":2,\"amount\":100}", "{\"status\":2,\"toAccountId\":999}", "{\"validTill\":null}", "{}" })
    void duesChangesFailBeforeAnyFieldMutation(String payload) {
        var instruction = existingDues();
        assertThrows(PlatformApiDataValidationException.class, () -> instruction.update(command(payload)));
        assertThat(instruction.instructionType()).isEqualTo(2);
        assertThat(instruction.amount()).isNull();
        assertThat(ReflectionTestUtils.getField(instruction, "status")).isEqualTo(1);
        assertThat(ReflectionTestUtils.getField(instruction, "priority")).isEqualTo(1);
    }

    @Test
    void suspensionAndCancellationRemainAvailable() {
        var instruction = existingDues();
        assertThat(instruction.update(command("{\"status\":2,\"locale\":\"en\"}"))).containsEntry("status", 2);
        assertThat(instruction.update(command("{\"status\":2,\"locale\":\"en\"}"))).isEmpty();
        assertThrows(PlatformApiDataValidationException.class, () -> instruction.update(command("{\"status\":1}")));
        instruction.delete();
        assertThat(ReflectionTestUtils.getField(instruction, "status")).isEqualTo(3);
    }

    @Test
    void internalFactoryCannotCreateOrCloneDues() {
        assertThrows(PlatformApiDataValidationException.class, () -> AccountTransferStandingInstruction
                .create(mock(AccountTransferDetails.class), "clone", 1, 2, 1, null, LocalDate.of(2025, 1, 1), null, 2, null, null, null));
    }

    @Test
    void fixedToDuesRejected() {
        var instruction = existingDues();
        ReflectionTestUtils.setField(instruction, "instructionType", 1);
        ReflectionTestUtils.setField(instruction, "amount", new BigDecimal("25"));
        assertThrows(PlatformApiDataValidationException.class, () -> instruction.update(command("{\"instructionType\":2}")));
        assertThat(instruction.instructionType()).isEqualTo(1);
        assertThat(instruction.amount()).isEqualByComparingTo("25");
    }

    private AccountTransferStandingInstruction existingDues() {
        AccountTransferDetails details = mock(AccountTransferDetails.class);
        when(details.transferType()).thenReturn(AccountTransferType.LOAN_REPAYMENT);
        when(details.fromSavingsAccount()).thenReturn(mock(SavingsAccount.class));
        when(details.toLoanAccount()).thenReturn(mock(Loan.class));
        // Hydrate historical state; the new-instruction factory must reject DUES.
        var instruction = new AccountTransferStandingInstruction();
        ReflectionTestUtils.setField(instruction, "accountTransferDetails", details);
        ReflectionTestUtils.setField(instruction, "name", "existing");
        ReflectionTestUtils.setField(instruction, "priority", 1);
        ReflectionTestUtils.setField(instruction, "instructionType", 2);
        ReflectionTestUtils.setField(instruction, "status", 1);
        ReflectionTestUtils.setField(instruction, "validFrom", LocalDate.of(2025, 1, 1));
        ReflectionTestUtils.setField(instruction, "recurrenceType", 2);
        return instruction;
    }

    private JsonCommand command(String payload) {
        return new JsonCommand(1L, json.parse(payload), json);
    }
}
