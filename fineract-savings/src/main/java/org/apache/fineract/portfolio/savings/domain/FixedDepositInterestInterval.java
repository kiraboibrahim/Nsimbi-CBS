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

public enum FixedDepositInterestInterval {
    MONTHLY("monthly"),
    YEARLY("yearly"),
    NOT_APPLICABLE("Not applicable");

    private final String value;

    FixedDepositInterestInterval(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static FixedDepositInterestInterval fromString(final String value) {
        if (value == null) {
            return null;
        }
        for (FixedDepositInterestInterval interval : values()) {
            if (interval.name().equalsIgnoreCase(value) || interval.value.equalsIgnoreCase(value)) {
                return interval;
            }
        }
        return null;
    }
}
