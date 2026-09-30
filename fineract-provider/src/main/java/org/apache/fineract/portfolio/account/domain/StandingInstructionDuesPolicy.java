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

import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

/** Setup restrictions; existing scheduler execution does not pass through this policy. */
public final class StandingInstructionDuesPolicy {

    private static final Set<String> SUSPENSION_PARAMETERS = Set.of("status", "locale", "dateFormat");

    private StandingInstructionDuesPolicy() {}

    public static void validateCreation(JsonCommand command) {
        validateCreation(command.integerValueSansLocaleOfParameterNamed("instructionType"));
    }

    public static void validateCreation(Integer instructionType) {
        if (StandingInstructionType.DUES.getValue().equals(instructionType)) {
            throw unsupported();
        }
    }

    public static void validateUpdate(Integer currentType, JsonCommand command) {
        if (StandingInstructionType.DUES.getValue().equals(currentType)) {
            if (!StandingInstructionStatus.DISABLED.getValue().equals(command.integerValueSansLocaleOfParameterNamed("status"))
                    || !SUSPENSION_PARAMETERS.containsAll(command.parsedJson().getAsJsonObject().keySet())) {
                throw unsupported();
            }
        } else {
            validateCreation(command);
        }
    }

    private static PlatformApiDataValidationException unsupported() {
        return new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError(
                "error.msg.standinginstruction.dues.requires.maximum",
                "DUES instructions require an enforceable maximum. New creation and material changes are unsupported. "
                        + "Cancel pending creation/change commands and resubmit a new fixed-amount instruction. "
                        + "Existing DUES instructions may only be suspended or cancelled. Disable automatic DUES creation on a loan before disbursement.",
                "instructionType", StandingInstructionType.DUES.getValue())));
    }
}
