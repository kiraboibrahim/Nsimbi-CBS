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
package org.apache.fineract.useradministration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_appuser_transaction_limit", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "appuser_id", "limit_type" }, name = "uq_appuser_tx_limit_user_type") })
@Getter
@Setter
@NoArgsConstructor
public class UserTransactionLimit extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appuser_id", nullable = false)
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false, length = 50)
    private TransactionLimitType limitType;

    @Column(name = "min_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal maxAmount;

    public UserTransactionLimit(final AppUser appUser, final TransactionLimitType limitType, final BigDecimal minAmount,
            final BigDecimal maxAmount) {
        this.appUser = appUser;
        this.limitType = limitType;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public void update(final BigDecimal minAmount, final BigDecimal maxAmount) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }
}
