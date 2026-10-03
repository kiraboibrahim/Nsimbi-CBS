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
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_debit_card_issuances")
@Getter
@Setter
@NoArgsConstructor
public class DebitCardIssuance extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount savingsAccount;

    @Column(name = "card_pan_masked", length = 30, nullable = false)
    private String cardPanMasked;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private DebitCardIssuanceStatus status;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id")
    private AppUser requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private AppUser approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id")
    private AppUser issuedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private AppUser rejectedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    public static DebitCardIssuance create(final SavingsAccount savingsAccount, final String cardPanMasked, final AppUser requestedBy) {
        final DebitCardIssuance issuance = new DebitCardIssuance();
        issuance.setSavingsAccount(savingsAccount);
        issuance.setCardPanMasked(cardPanMasked);
        issuance.setStatus(DebitCardIssuanceStatus.PENDING);
        issuance.setRequestedBy(requestedBy);
        issuance.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        return issuance;
    }

    public boolean isPending() {
        return DebitCardIssuanceStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return DebitCardIssuanceStatus.APPROVED.equals(this.status);
    }

    public boolean isIssued() {
        return DebitCardIssuanceStatus.ISSUED.equals(this.status);
    }

    public void approve(final AppUser approvedBy) {
        this.status = DebitCardIssuanceStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void issue(final AppUser issuedBy) {
        this.status = DebitCardIssuanceStatus.ISSUED;
        this.issuedBy = issuedBy;
        this.issuedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void reject(final AppUser rejectedBy, final String reason) {
        this.status = DebitCardIssuanceStatus.REJECTED;
        this.rejectedBy = rejectedBy;
        this.rejectionReason = reason;
        this.rejectedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
