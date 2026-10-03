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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.DebitCardIssuanceDataValidator;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuance;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuanceRepository;
import org.apache.fineract.portfolio.savings.domain.DebitCardIssuanceStatus;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DebitCardIssuanceWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private DebitCardIssuanceRepository debitCardIssuanceRepository;
    @Mock
    private DebitCardIssuanceDataValidator debitCardIssuanceDataValidator;
    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    private final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();

    private DebitCardIssuanceWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new DebitCardIssuanceWritePlatformServiceJpaRepositoryImpl(
                context, fromApiJsonHelper, debitCardIssuanceRepository,
                debitCardIssuanceDataValidator, savingsAccountRepositoryWrapper);
    }

    @Test
    void testRequestDebitCard_Success() {
        AppUser currentUser = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(currentUser);

        SavingsAccount account = mock(SavingsAccount.class);
        when(account.clientId()).thenReturn(10L);
        when(account.officeId()).thenReturn(1L);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(account);

        when(debitCardIssuanceRepository.save(any(DebitCardIssuance.class))).thenAnswer(invocation -> {
            DebitCardIssuance card = invocation.getArgument(0);
            card.setId(77L);
            return card;
        });

        String json = "{\n"
                + "  \"savingsAccountId\": 1,\n"
                + "  \"cardPanMasked\": \"5061********1234\"\n"
                + "}";

        JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn(json);

        CommandProcessingResult result = service.requestDebitCard(command);

        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isEqualTo(77L);
        assertThat(result.getClientId()).isEqualTo(10L);
        verify(debitCardIssuanceDataValidator).validateForRequest(json);
    }

    @Test
    void testApproveDebitCard_SelfApprovalBlocked_ThrowsException() {
        AppUser requisitioner = mock(AppUser.class);
        when(requisitioner.getId()).thenReturn(3L);

        DebitCardIssuance card = new DebitCardIssuance();
        card.setId(20L);
        card.setStatus(DebitCardIssuanceStatus.PENDING);
        card.setRequestedBy(requisitioner);

        when(debitCardIssuanceRepository.findById(20L)).thenReturn(Optional.of(card));

        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(3L);
        when(context.authenticatedUser()).thenReturn(approver);

        JsonCommand command = mock(JsonCommand.class);

        assertThatThrownBy(() -> service.approveDebitCard(20L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Requisitioner of the debit card cannot approve it");
    }

    @Test
    void testApproveDebitCard_DifferentUser_SetsApproved() {
        AppUser requisitioner = mock(AppUser.class);
        when(requisitioner.getId()).thenReturn(3L);

        SavingsAccount account = mock(SavingsAccount.class);
        when(account.clientId()).thenReturn(15L);

        DebitCardIssuance card = new DebitCardIssuance();
        card.setId(21L);
        card.setStatus(DebitCardIssuanceStatus.PENDING);
        card.setRequestedBy(requisitioner);
        card.setSavingsAccount(account);

        when(debitCardIssuanceRepository.findById(21L)).thenReturn(Optional.of(card));

        AppUser approver = mock(AppUser.class);
        when(approver.getId()).thenReturn(8L);
        when(context.authenticatedUser()).thenReturn(approver);

        JsonCommand command = mock(JsonCommand.class);

        CommandProcessingResult result = service.approveDebitCard(21L, command);

        assertThat(result).isNotNull();
        assertThat(card.getStatus()).isEqualTo(DebitCardIssuanceStatus.APPROVED);
        assertThat(card.getApprovedBy()).isEqualTo(approver);
        assertThat(card.getApprovedAt()).isNotNull();
    }

    @Test
    void testIssueDebitCard_ApprovedCard_SetsIssued() {
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.clientId()).thenReturn(15L);

        DebitCardIssuance card = new DebitCardIssuance();
        card.setId(22L);
        card.setStatus(DebitCardIssuanceStatus.APPROVED);
        card.setSavingsAccount(account);

        when(debitCardIssuanceRepository.findById(22L)).thenReturn(Optional.of(card));

        AppUser issuer = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(issuer);

        JsonCommand command = mock(JsonCommand.class);

        CommandProcessingResult result = service.issueDebitCard(22L, command);

        assertThat(result).isNotNull();
        assertThat(card.getStatus()).isEqualTo(DebitCardIssuanceStatus.ISSUED);
        assertThat(card.getIssuedBy()).isEqualTo(issuer);
        assertThat(card.getIssuedAt()).isNotNull();
    }

    @Test
    void testIssueDebitCard_PendingCard_ThrowsException() {
        DebitCardIssuance card = new DebitCardIssuance();
        card.setId(23L);
        card.setStatus(DebitCardIssuanceStatus.PENDING);

        when(debitCardIssuanceRepository.findById(23L)).thenReturn(Optional.of(card));

        JsonCommand command = mock(JsonCommand.class);

        assertThatThrownBy(() -> service.issueDebitCard(23L, command))
                .isInstanceOf(GeneralPlatformDomainRuleException.class)
                .hasMessageContaining("Only APPROVED debit card requisitions can be issued");
    }

    @Test
    void testRejectDebitCard_SetsRejected() {
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.clientId()).thenReturn(15L);

        DebitCardIssuance card = new DebitCardIssuance();
        card.setId(24L);
        card.setStatus(DebitCardIssuanceStatus.PENDING);
        card.setSavingsAccount(account);

        when(debitCardIssuanceRepository.findById(24L)).thenReturn(Optional.of(card));

        AppUser rejecter = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(rejecter);

        JsonCommand command = mock(JsonCommand.class);
        when(command.stringValueOfParameterNamed("rejectionReason")).thenReturn("Invalid account details");

        CommandProcessingResult result = service.rejectDebitCard(24L, command);

        assertThat(result).isNotNull();
        assertThat(card.getStatus()).isEqualTo(DebitCardIssuanceStatus.REJECTED);
        assertThat(card.getRejectionReason()).isEqualTo("Invalid account details");
        assertThat(card.getRejectedAt()).isNotNull();
    }
}
