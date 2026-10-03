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

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.savings.data.FixedDepositData;
import org.apache.fineract.portfolio.savings.domain.FixedDeposit;
import org.apache.fineract.portfolio.savings.domain.FixedDepositRepository;
import org.apache.fineract.portfolio.savings.domain.FixedDepositStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FixedDepositReadPlatformServiceImpl implements FixedDepositReadPlatformService {

    private final FixedDepositRepository fixedDepositRepository;

    @Override
    public Page<FixedDepositData> retrieveAll(final SearchParameters searchParameters) {
        final List<FixedDeposit> allEntities;

        if (searchParameters != null && StringUtils.isNotBlank(searchParameters.getStatus())
                && !"all".equalsIgnoreCase(searchParameters.getStatus())) {
            final FixedDepositStatus status = FixedDepositStatus.fromString(searchParameters.getStatus());
            if (status != null) {
                allEntities = this.fixedDepositRepository.findByStatus(status);
            } else {
                allEntities = this.fixedDepositRepository.findAll();
            }
        } else {
            allEntities = this.fixedDepositRepository.findAll();
        }

        final List<FixedDepositData> dataList = new ArrayList<>();
        for (FixedDeposit entity : allEntities) {
            dataList.add(mapToData(entity));
        }

        return new Page<>(dataList, dataList.size());
    }

    @Override
    public FixedDepositData retrieveOne(final Long id) {
        final FixedDeposit entity = this.fixedDepositRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.fixeddeposit.not.found",
                        "Fixed deposit not found with id " + id));
        return mapToData(entity);
    }

    @Override
    public List<FixedDepositData> retrieveByCustomerId(final Long customerId) {
        final List<FixedDeposit> entities = this.fixedDepositRepository.findByCustomerId(customerId);
        final List<FixedDepositData> result = new ArrayList<>();
        for (FixedDeposit entity : entities) {
            result.add(mapToData(entity));
        }
        return result;
    }

    private FixedDepositData mapToData(final FixedDeposit entity) {
        return FixedDepositData.builder() //
                .id(entity.getId()) //
                .depositNumber(entity.getDepositNumber()) //
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null) //
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getDisplayName() : null) //
                .fundingSource(entity.getFundingSource() != null ? entity.getFundingSource().name() : null) //
                .fundingSavingsAccountId(entity.getFundingSavingsAccount() != null ? entity.getFundingSavingsAccount().getId() : null) //
                .fundingSavingsAccountNumber(
                        entity.getFundingSavingsAccount() != null ? entity.getFundingSavingsAccount().getAccountNumber() : null) //
                .payoutSavingsAccountId(entity.getPayoutSavingsAccount() != null ? entity.getPayoutSavingsAccount().getId() : null) //
                .payoutSavingsAccountNumber(
                        entity.getPayoutSavingsAccount() != null ? entity.getPayoutSavingsAccount().getAccountNumber() : null) //
                .depositorName(entity.getDepositorName()) //
                .depositorPhone(entity.getDepositorPhone()) //
                .principalAmount(entity.getPrincipalAmount()) //
                .startsOn(entity.getStartsOn()) //
                .periodMonths(entity.getPeriodMonths()) //
                .interestRateAnnual(entity.getInterestRateAnnual()) //
                .interestInterval(entity.getInterestInterval() != null ? entity.getInterestInterval().getValue() : null) //
                .payoutOption(entity.getPayoutOption() != null ? entity.getPayoutOption().getValue() : null) //
                .status(entity.getStatus() != null ? entity.getStatus().name() : null) //
                .maturesAt(entity.getMaturesAt()) //
                .accruedInterest(entity.getAccruedInterest()) //
                .createdAt(entity.getCreatedAt()) //
                .build();
    }
}
