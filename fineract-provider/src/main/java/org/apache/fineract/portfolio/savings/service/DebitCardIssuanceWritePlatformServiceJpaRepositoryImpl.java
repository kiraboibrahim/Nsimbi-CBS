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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.DebitCardIssuanceDataValidator;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuance;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuanceRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebitCardIssuanceWritePlatformServiceJpaRepositoryImpl implements DebitCardIssuanceWritePlatformService {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final DebitCardIssuanceRepository debitCardIssuanceRepository;
    private final DebitCardIssuanceDataValidator debitCardIssuanceDataValidator;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    @Transactional
    @Override
    public CommandProcessingResult requestDebitCard(final JsonCommand command) {
        this.debitCardIssuanceDataValidator.validateForRequest(command.json());

        final AppUser currentUser = this.context.authenticatedUser();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());

        final Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed("savingsAccountId", element);
        final String cardPanMasked = this.fromApiJsonHelper.extractStringNamed("cardPanMasked", element);

        final SavingsAccount account = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);

        final DebitCardIssuance issuance = DebitCardIssuance.create(account, cardPanMasked, currentUser);
        this.debitCardIssuanceRepository.save(issuance);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(issuance.getId()) //
                .withClientId(account.clientId()) //
                .withOfficeId(account.officeId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveDebitCard(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final DebitCardIssuance issuance = this.debitCardIssuanceRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.debitcard.not.found",
                        "Debit card issuance not found with id " + id));

        if (!issuance.isPending()) {
            throw new GeneralPlatformDomainRuleException("error.msg.debitcard.not.pending",
                    "Debit card issuance cannot be approved in current status: " + issuance.getStatus().name());
        }

        // Segregation of duties: Maker cannot approve their own card requisition
        if (issuance.getRequestedBy() != null && issuance.getRequestedBy().getId().equals(currentUser.getId())) {
            throw new GeneralPlatformDomainRuleException("error.msg.debitcard.maker.cannot.approve",
                    "Requisitioner of the debit card cannot approve it");
        }

        issuance.approve(currentUser);
        this.debitCardIssuanceRepository.save(issuance);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(issuance.getId()) //
                .withClientId(issuance.getSavingsAccount().clientId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult issueDebitCard(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final DebitCardIssuance issuance = this.debitCardIssuanceRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.debitcard.not.found",
                        "Debit card issuance not found with id " + id));

        if (!issuance.isApproved()) {
            throw new GeneralPlatformDomainRuleException("error.msg.debitcard.not.approved",
                    "Only APPROVED debit card requisitions can be issued, current status: " + issuance.getStatus().name());
        }

        issuance.issue(currentUser);
        this.debitCardIssuanceRepository.save(issuance);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(issuance.getId()) //
                .withClientId(issuance.getSavingsAccount().clientId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectDebitCard(final Long id, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final DebitCardIssuance issuance = this.debitCardIssuanceRepository.findById(id)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.debitcard.not.found",
                        "Debit card issuance not found with id " + id));

        if (issuance.isIssued()) {
            throw new GeneralPlatformDomainRuleException("error.msg.debitcard.already.issued",
                    "Debit card has already been issued and cannot be rejected");
        }

        final String reason = command.stringValueOfParameterNamed("rejectionReason");
        issuance.reject(currentUser, reason);
        this.debitCardIssuanceRepository.save(issuance);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(issuance.getId()) //
                .withClientId(issuance.getSavingsAccount().clientId()) //
                .build();
    }
}
