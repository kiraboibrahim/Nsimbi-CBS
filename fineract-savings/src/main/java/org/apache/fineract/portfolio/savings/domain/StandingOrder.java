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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_standing_orders")
@Getter
@Setter
@NoArgsConstructor
public class StandingOrder extends AbstractPersistableCustom<Long> {

    @Column(name = "order_number", length = 50, nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private SavingsAccount sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private SavingsAccount destinationAccount;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 30, nullable = false)
    private StandingOrderFrequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private StandingOrderStatus status;

    @Column(name = "last_run_date")
    private LocalDate lastRunDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private AppUser approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private AppUser rejectedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    public static StandingOrder create(final String orderNumber, final String name,
            final SavingsAccount sourceAccount, final SavingsAccount destinationAccount,
            final BigDecimal amount, final StandingOrderFrequency frequency,
            final LocalDate startDate, final LocalDate endDate, final AppUser createdBy) {
        final StandingOrder order = new StandingOrder();
        order.setOrderNumber(orderNumber);
        order.setName(name);
        order.setSourceAccount(sourceAccount);
        order.setDestinationAccount(destinationAccount);
        order.setAmount(amount);
        order.setFrequency(frequency);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setStatus(StandingOrderStatus.PENDING);
        order.setCreatedBy(createdBy);
        order.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        return order;
    }

    public boolean isPending() {
        return StandingOrderStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return StandingOrderStatus.APPROVED.equals(this.status);
    }

    public boolean isRunning() {
        return StandingOrderStatus.RUNNING.equals(this.status);
    }

    public void approve(final AppUser approvedBy) {
        this.status = StandingOrderStatus.RUNNING;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void reject(final AppUser rejectedBy, final String reason) {
        this.status = StandingOrderStatus.REJECTED;
        this.rejectedBy = rejectedBy;
        this.rejectionReason = reason;
        this.rejectedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void expire() {
        this.status = StandingOrderStatus.EXPIRED;
    }

    public void recordExecution(final LocalDate runDate) {
        this.lastRunDate = runDate;
        this.status = StandingOrderStatus.RUNNING;
    }
}
