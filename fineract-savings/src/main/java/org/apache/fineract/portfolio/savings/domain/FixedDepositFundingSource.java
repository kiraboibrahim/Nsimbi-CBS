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

public enum FixedDepositFundingSource {
    TILL_CASH("TILL_CASH"),
    SAVINGS_OFFSET("SAVINGS_OFFSET");

    private final String value;

    FixedDepositFundingSource(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static FixedDepositFundingSource fromString(final String value) {
        if (value == null) {
            return null;
        }
        for (FixedDepositFundingSource source : values()) {
            if (source.name().equalsIgnoreCase(value) || source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        if (value.toLowerCase().contains("till") || value.toLowerCase().contains("cash")) {
            return TILL_CASH;
        }
        if (value.toLowerCase().contains("offset") || value.toLowerCase().contains("savings")) {
            return SAVINGS_OFFSET;
        }
        return null;
    }
}
