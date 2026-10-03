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
package org.apache.fineract.portfolio.savings.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.savings.data.DebitCardIssuanceData;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuance;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuanceRepository;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuanceStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DebitCardIssuanceReadPlatformServiceImpl implements DebitCardIssuanceReadPlatformService {

    private final DebitCardIssuanceRepository debitCardIssuanceRepository;

    @Override
    public List<DebitCardIssuanceData> retrieveAll(final String status, final Long savingsAccountId) {
        List<DebitCardIssuance> issuances;
        if (StringUtils.isNotBlank(status)) {
            final DebitCardIssuanceStatus cardStatus = DebitCardIssuanceStatus.fromString(status);
            issuances = cardStatus != null ? this.debitCardIssuanceRepository.findByStatus(cardStatus)
                    : this.debitCardIssuanceRepository.findAll();
        } else if (savingsAccountId != null) {
            issuances = this.debitCardIssuanceRepository.findBySavingsAccountId(savingsAccountId);
        } else {
            issuances = this.debitCardIssuanceRepository.findAll();
        }

        return issuances.stream().map(this::toData).collect(Collectors.toList());
    }

    @Override
    public DebitCardIssuanceData retrieveOne(final Long id) {
        final DebitCardIssuance issuance = this.debitCardIssuanceRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.debitcard.not.found",
                        "Debit card issuance not found with id " + id));
        return toData(issuance);
    }

    private DebitCardIssuanceData toData(final DebitCardIssuance issuance) {
        return DebitCardIssuanceData.builder()
                .id(issuance.getId())
                .savingsAccountId(issuance.getSavingsAccount().getId())
                .savingsAccountNo(issuance.getSavingsAccount().getAccountNumber())
                .cardPanMasked(issuance.getCardPanMasked())
                .status(issuance.getStatus() != null ? issuance.getStatus().name() : null)
                .rejectionReason(issuance.getRejectionReason())
                .requestedBy(issuance.getRequestedBy() != null ? issuance.getRequestedBy().getUsername() : null)
                .approvedBy(issuance.getApprovedBy() != null ? issuance.getApprovedBy().getUsername() : null)
                .issuedBy(issuance.getIssuedBy() != null ? issuance.getIssuedBy().getUsername() : null)
                .createdAt(issuance.getCreatedAt())
                .approvedAt(issuance.getApprovedAt())
                .issuedAt(issuance.getIssuedAt())
                .build();
    }
}
