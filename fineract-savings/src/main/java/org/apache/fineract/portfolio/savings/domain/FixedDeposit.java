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
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_fixed_deposits")
@Getter
@Setter
@NoArgsConstructor
public class FixedDeposit extends AbstractPersistableCustom<Long> {

    @Column(name = "deposit_number", length = 50, nullable = false, unique = true)
    private String depositNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Client customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "funding_source", length = 30, nullable = false)
    private FixedDepositFundingSource fundingSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_savings_account_id")
    private SavingsAccount fundingSavingsAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_savings_account_id", nullable = false)
    private SavingsAccount payoutSavingsAccount;

    @Column(name = "depositor_name", length = 150, nullable = false)
    private String depositorName;

    @Column(name = "depositor_phone", length = 30, nullable = false)
    private String depositorPhone;

    @Column(name = "principal_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "period_months", nullable = false)
    private Integer periodMonths;

    @Column(name = "interest_rate_annual", precision = 5, scale = 2, nullable = false)
    private BigDecimal interestRateAnnual;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_interval", length = 30, nullable = false)
    private FixedDepositInterestInterval interestInterval;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_option", length = 50, nullable = false)
    private FixedDepositPayoutOption payoutOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private FixedDepositStatus status = FixedDepositStatus.PENDING;

    @Column(name = "matures_at", nullable = false)
    private LocalDate maturesAt;

    @Column(name = "accrued_interest", precision = 18, scale = 2, nullable = false)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.systemDefault());

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private AppUser approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private AppUser rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_id")
    private AppUser closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public static FixedDeposit create(final String depositNumber, final Client customer,
            final FixedDepositFundingSource fundingSource, final SavingsAccount fundingSavingsAccount,
            final SavingsAccount payoutSavingsAccount, final String depositorName, final String depositorPhone,
            final BigDecimal principalAmount, final LocalDate startsOn, final Integer periodMonths,
            final BigDecimal interestRateAnnual, final FixedDepositInterestInterval interestInterval,
            final FixedDepositPayoutOption payoutOption, final AppUser createdBy) {
        final FixedDeposit fd = new FixedDeposit();
        fd.setDepositNumber(depositNumber);
        fd.setCustomer(customer);
        fd.setFundingSource(fundingSource);
        fd.setFundingSavingsAccount(fundingSavingsAccount);
        fd.setPayoutSavingsAccount(payoutSavingsAccount);
        fd.setDepositorName(depositorName);
        fd.setDepositorPhone(depositorPhone);
        fd.setPrincipalAmount(principalAmount);
        fd.setStartsOn(startsOn);
        fd.setPeriodMonths(periodMonths);
        fd.setInterestRateAnnual(interestRateAnnual);
        fd.setInterestInterval(interestInterval);
        fd.setPayoutOption(payoutOption);
        fd.setStatus(FixedDepositStatus.PENDING);
        fd.setMaturesAt(startsOn != null && periodMonths != null ? startsOn.plusMonths(periodMonths) : null);
        fd.setAccruedInterest(BigDecimal.ZERO);
        fd.setCreatedBy(createdBy);
        fd.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        return fd;
    }

    public boolean isPending() {
        return FixedDepositStatus.PENDING.equals(this.status);
    }

    public boolean isRunning() {
        return FixedDepositStatus.RUNNING.equals(this.status);
    }

    public void approve(final AppUser approvedBy) {
        this.status = FixedDepositStatus.RUNNING;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void reject(final AppUser rejectedBy, final String rejectionReason) {
        this.status = FixedDepositStatus.REJECTED;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = LocalDateTime.now(ZoneId.systemDefault());
        this.rejectionReason = rejectionReason;
    }

    public void terminate(final FixedDepositStatus terminalStatus, final BigDecimal finalAccruedInterest, final AppUser closedBy) {
        this.status = terminalStatus;
        this.accruedInterest = finalAccruedInterest;
        this.closedBy = closedBy;
        this.closedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
