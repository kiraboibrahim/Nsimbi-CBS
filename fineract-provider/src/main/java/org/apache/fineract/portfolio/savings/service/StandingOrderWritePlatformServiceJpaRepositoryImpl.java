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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.data.StandingOrderDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.StandingOrder;
import org.apache.fineract.portfolio.savings.domain.StandingOrderFrequency;
import org.apache.fineract.portfolio.savings.domain.StandingOrderRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandingOrderWritePlatformServiceJpaRepositoryImpl implements StandingOrderWritePlatformService {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderDataValidator standingOrderDataValidator;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;

    @Transactional
    @Override
    public CommandProcessingResult createStandingOrder(final JsonCommand command) {
        this.standingOrderDataValidator.validateForCreate(command.json());

        final AppUser currentUser = this.context.authenticatedUser();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        final Long sourceAccountId = this.fromApiJsonHelper.extractLongNamed("sourceAccountId", element);
        final Long destinationAccountId = this.fromApiJsonHelper.extractLongNamed("destinationAccountId", element);
        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        final String frequencyStr = this.fromApiJsonHelper.extractStringNamed("frequency", element);
        final StandingOrderFrequency frequency = StandingOrderFrequency.fromString(frequencyStr);
        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed("startDate", element);
        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed("endDate", element);

        final SavingsAccount sourceAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(sourceAccountId);
        final SavingsAccount destinationAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(destinationAccountId);

        final String orderNumber = "SO" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        final StandingOrder order = StandingOrder.create(orderNumber, name, sourceAccount, destinationAccount,
                amount, frequency, startDate, endDate, currentUser);

        this.standingOrderRepository.save(order);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(order.getId()) //
                .withClientId(sourceAccount.clientId()) //
                .withOfficeId(sourceAccount.officeId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveStandingOrder(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final StandingOrder order = this.standingOrderRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.standingorder.not.found",
                        "Standing order not found with id " + id));

        if (!order.isPending()) {
            throw new GeneralPlatformDomainRuleException("error.msg.standingorder.not.pending",
                    "Standing order cannot be approved in current status: " + order.getStatus().name());
        }

        // Segregation of duties: Maker cannot approve their own standing order
        if (order.getCreatedBy() != null && order.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new GeneralPlatformDomainRuleException("error.msg.standingorder.maker.cannot.approve",
                    "Maker of the standing order cannot approve it");
        }

        order.approve(currentUser);
        this.standingOrderRepository.save(order);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(order.getId()) //
                .withClientId(order.getSourceAccount().clientId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectStandingOrder(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final StandingOrder order = this.standingOrderRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.standingorder.not.found",
                        "Standing order not found with id " + id));

        if (!order.isPending()) {
            throw new GeneralPlatformDomainRuleException("error.msg.standingorder.not.pending",
                    "Standing order cannot be rejected in current status: " + order.getStatus().name());
        }

        final String reason = command.stringValueOfParameterNamed("rejectionReason");
        order.reject(currentUser, reason);
        this.standingOrderRepository.save(order);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(order.getId()) //
                .withClientId(order.getSourceAccount().clientId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult executeStandingOrder(final Long id) {
        final StandingOrder order = this.standingOrderRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.standingorder.not.found",
                        "Standing order not found with id " + id));

        if (!order.isRunning() && !order.isApproved()) {
            throw new GeneralPlatformDomainRuleException("error.msg.standingorder.not.executable",
                    "Standing order cannot be executed in current status: " + order.getStatus().name());
        }

        final SavingsAccount sourceAccount = order.getSourceAccount();
        final SavingsAccount destinationAccount = order.getDestinationAccount();

        // Check available balance on source account (Actual - Reserve 5000 - Hold)
        final BigDecimal availableBalance = this.standingOrderDataValidator.calculateAvailableBalance(sourceAccount);
        if (availableBalance.compareTo(order.getAmount()) < 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.standingorder.insufficient.balance",
                    "Insufficient available balance in source savings account to execute standing order");
        }

        final LocalDate executionDate = LocalDate.now();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final boolean isAccountTransfer = true;
        final boolean isRegularTransaction = true;
        final boolean isApplyWithdrawFee = false;
        final boolean isInterestTransfer = false;
        final boolean isWithdrawBalance = false;
        final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                isRegularTransaction, isApplyWithdrawFee, isInterestTransfer, isWithdrawBalance);

        // Debit source account
        final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(sourceAccount, fmt,
                executionDate, order.getAmount(), null, transactionBooleanValues, false);
        this.savingsAccountTransactionRepository.save(withdrawal);

        // Credit destination account
        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(destinationAccount, fmt,
                executionDate, order.getAmount(), null, isAccountTransfer, isRegularTransaction, false);
        this.savingsAccountTransactionRepository.save(deposit);

        if (order.getEndDate() != null && executionDate.isAfter(order.getEndDate())) {
            order.expire();
        } else {
            order.recordExecution(executionDate);
        }
        this.standingOrderRepository.save(order);

        return new CommandProcessingResultBuilder() //
                .withEntityId(order.getId()) //
                .withClientId(sourceAccount.clientId()) //
                .build();
    }
}
