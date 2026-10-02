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

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserTransactionLimitData implements Serializable {

    private final Long id;
    private final Long userId;
    private final String limitType;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;

    public static UserTransactionLimitData instance(final Long id, final Long userId, final String limitType, final BigDecimal minAmount,
            final BigDecimal maxAmount) {
        return new UserTransactionLimitData(id, userId, limitType, minAmount, maxAmount);
    }
}
