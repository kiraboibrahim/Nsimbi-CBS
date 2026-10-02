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
package org.apache.fineract.portfolio.client.domain;

import lombok.Getter;

@Getter
public enum CustomerType {

    INDIVIDUAL(1, "customerType.individual", "Individual"),
    GROUP(2, "customerType.group", "Group"),
    JOINT(3, "customerType.joint", "Joint"),
    INSTITUTION(4, "customerType.institution", "Institution");

    private final Integer value;
    private final String code;
    private final String label;

    CustomerType(final Integer value, final String code, final String label) {
        this.value = value;
        this.code = code;
        this.label = label;
    }

    public static CustomerType fromInt(final Integer type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case 1 -> CustomerType.INDIVIDUAL;
            case 2 -> CustomerType.GROUP;
            case 3 -> CustomerType.JOINT;
            case 4 -> CustomerType.INSTITUTION;
            default -> null;
        };
    }

    public static CustomerType fromString(final String typeStr) {
        if (typeStr == null) {
            return null;
        }
        for (CustomerType type : CustomerType.values()) {
            if (type.name().equalsIgnoreCase(typeStr.trim()) || type.label.equalsIgnoreCase(typeStr.trim())) {
                return type;
            }
        }
        return null;
    }

    public boolean isIndividual() {
        return this.equals(INDIVIDUAL);
    }

    public boolean isGroup() {
        return this.equals(GROUP);
    }

    public boolean isJoint() {
        return this.equals(JOINT);
    }

    public boolean isInstitution() {
        return this.equals(INSTITUTION);
    }

    @Override
    public String toString() {
        return this.name();
    }
}
