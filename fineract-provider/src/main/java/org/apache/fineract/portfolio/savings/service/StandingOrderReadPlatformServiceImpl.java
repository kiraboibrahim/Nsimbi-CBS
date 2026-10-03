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
import org.apache.fineract.portfolio.savings.data.StandingOrderData;
import org.apache.fineract.portfolio.savings.domain.StandingOrder;
import org.apache.fineract.portfolio.savings.domain.StandingOrderRepository;
import org.apache.fineract.portfolio.savings.domain.StandingOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StandingOrderReadPlatformServiceImpl implements StandingOrderReadPlatformService {

    private final StandingOrderRepository standingOrderRepository;

    @Override
    public List<StandingOrderData> retrieveAll(final String status, final Long sourceAccountId) {
        List<StandingOrder> orders;
        if (StringUtils.isNotBlank(status)) {
            final StandingOrderStatus orderStatus = StandingOrderStatus.fromString(status);
            orders = orderStatus != null ? this.standingOrderRepository.findByStatus(orderStatus) : this.standingOrderRepository.findAll();
        } else if (sourceAccountId != null) {
            orders = this.standingOrderRepository.findBySourceAccountId(sourceAccountId);
        } else {
            orders = this.standingOrderRepository.findAll();
        }

        return orders.stream().map(this::toData).collect(Collectors.toList());
    }

    @Override
    public StandingOrderData retrieveOne(final Long id) {
        final StandingOrder order = this.standingOrderRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.standingorder.not.found",
                        "Standing order not found with id " + id));
        return toData(order);
    }

    private StandingOrderData toData(final StandingOrder order) {
        return StandingOrderData.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .name(order.getName())
                .sourceAccountId(order.getSourceAccount().getId())
                .sourceAccountNo(order.getSourceAccount().getAccountNumber())
                .destinationAccountId(order.getDestinationAccount().getId())
                .destinationAccountNo(order.getDestinationAccount().getAccountNumber())
                .amount(order.getAmount())
                .frequency(order.getFrequency() != null ? order.getFrequency().name() : null)
                .startDate(order.getStartDate())
                .endDate(order.getEndDate())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .lastRunDate(order.getLastRunDate())
                .rejectionReason(order.getRejectionReason())
                .build();
    }
}
