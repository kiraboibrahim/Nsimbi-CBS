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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataValidator;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsSmsAlertConfig;
import org.apache.fineract.portfolio.savings.domain.SavingsSmsAlertConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsApplicationProcessWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private SavingsAccountRepositoryWrapper savingAccountRepository;
    @Mock
    private SavingsAccountAssembler savingAccountAssembler;
    @Mock
    private SavingsAccountDataValidator savingsAccountDataValidator;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private SavingsProductRepository savingsProductRepository;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private StaffRepositoryWrapper staffRepository;
    @Mock
    private SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator;
    @Mock
    private SavingsAccountChargeAssembler savingsAccountChargeAssembler;
    @Mock
    private CommandProcessingService commandProcessingService;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;
    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @Mock
    private GSIMRepositoy gsimRepository;
    @Mock
    private GroupRepositoryWrapper groupRepositoryWrapper;
    @Mock
    private GroupSavingsIndividualMonitoringWritePlatformService gsimWritePlatformService;
    @Mock
    private SavingsSmsAlertConfigRepository savingsSmsAlertConfigRepository;

    private SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepository,
                savingAccountAssembler, savingsAccountDataValidator, accountNumberGenerator, clientRepository, groupRepository,
                savingsProductRepository, noteRepository, staffRepository, savingsAccountApplicationTransitionApiJsonValidator,
                savingsAccountChargeAssembler, commandProcessingService, savingsAccountDomainService, savingsAccountWritePlatformService,
                accountNumberFormatRepository, businessEventNotifierService, entityDatatableChecksWritePlatformService, gsimRepository,
                groupRepositoryWrapper, gsimWritePlatformService, savingsSmsAlertConfigRepository);
    }

    @Test
    void deleteApplication_shouldThrowExceptionWhenAccountHasNonZeroBalance() {
        Long savingsId = 1L;
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.isNotSubmittedAndPendingApproval()).thenReturn(false);
        when(account.getAccountBalance()).thenReturn(new BigDecimal("100.00"));
        when(savingAccountAssembler.assembleFrom(savingsId, false)).thenReturn(account);

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> service.deleteApplication(savingsId));

        assertThat(ex.getErrors()).hasSize(1);
        assertThat(ex.getErrors().get(0).getUserMessageGlobalisationCode()).contains("cannot.delete.account.with.non.zero.balance");
    }

    @Test
    void deleteApplication_shouldDeleteAccountAndSmsAlertConfigWhenBalanceIsZero() {
        Long savingsId = 1L;
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.getId()).thenReturn(savingsId);
        when(account.isNotSubmittedAndPendingApproval()).thenReturn(false);
        when(account.getAccountBalance()).thenReturn(BigDecimal.ZERO);
        when(savingAccountAssembler.assembleFrom(savingsId, false)).thenReturn(account);

        SavingsSmsAlertConfig alertConfig = mock(SavingsSmsAlertConfig.class);
        when(savingsSmsAlertConfigRepository.findBySavingsAccountId(savingsId)).thenReturn(Optional.of(alertConfig));

        CommandProcessingResult result = service.deleteApplication(savingsId);

        assertThat(result).isNotNull();
        assertThat(result.getSavingsId()).isEqualTo(savingsId);
        verify(savingsSmsAlertConfigRepository, times(1)).delete(alertConfig);
        verify(noteRepository, times(1)).deleteAllBySavingsAccount(account);
        verify(savingAccountRepository, times(1)).delete(account);
    }
}
