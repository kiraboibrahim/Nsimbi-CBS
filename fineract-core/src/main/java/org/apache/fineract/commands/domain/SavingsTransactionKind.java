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
package org.apache.fineract.commands.domain;

public enum SavingsTransactionKind {

    DEPOSIT, WITHDRAWAL, FORCE_WITHDRAWAL, ADJUSTTRANSACTION, CLOSE, GSIM_CLOSE, ACCOUNT_TRANSFER, ACCOUNT_TRANSFER_REFUND, STANDING_INSTRUCTION_CREATE, STANDING_INSTRUCTION_UPDATE;

    public static SavingsTransactionKind fromCommand(String action, String entity) {
        if ("ACCOUNTTRANSFER".equals(entity)) {
            return switch (action) {
                case "CREATE" -> ACCOUNT_TRANSFER;
                case "REFUNDBYTRANSFER" -> ACCOUNT_TRANSFER_REFUND;
                default -> null;
            };
        }
        if ("STANDINGINSTRUCTION".equals(entity)) {
            return switch (action) {
                case "CREATE" -> STANDING_INSTRUCTION_CREATE;
                case "UPDATE" -> STANDING_INSTRUCTION_UPDATE;
                default -> null;
            };
        }
        if ("GSIMACCOUNT".equals(entity) && "CLOSE".equals(action)) {
            return GSIM_CLOSE;
        }
        if (!"SAVINGSACCOUNT".equals(entity)) {
            return null;
        }
        for (var kind : values()) {
            if (!kind.isTransferOperation() && kind != GSIM_CLOSE && kind.name().equals(action)) {
                return kind;
            }
        }
        return null;
    }

    public boolean isTransferOperation() {
        return this == ACCOUNT_TRANSFER || this == ACCOUNT_TRANSFER_REFUND || this == STANDING_INSTRUCTION_CREATE
                || this == STANDING_INSTRUCTION_UPDATE;
    }

    public boolean accepts(SavingsTransactionOrigin origin) {
        return origin == SavingsTransactionOrigin.STAFF_API
                || origin == SavingsTransactionOrigin.SPREADSHEET_IMPORT && (this == DEPOSIT || this == WITHDRAWAL);
    }
}
