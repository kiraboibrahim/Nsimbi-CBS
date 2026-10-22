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
package org.apache.fineract.portfolio.collectionsheet.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.fineract.commands.domain.LoanCommandExecutionContext;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetTransactionDataValidator;
import org.apache.fineract.portfolio.collectionsheet.handler.SaveIndividualCollectionSheetCommandHandler;
import org.apache.fineract.portfolio.collectionsheet.handler.UpdateCollectionSheetCommandHandler;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionSheetLoanAuthorityTest {

    @Mock
    private LoanWritePlatformService loans;
    @Mock
    private CollectionSheetTransactionDataValidator validator;
    @Mock
    private CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer deserializer;
    @Mock
    private PaymentDetailWritePlatformService payments;
    @InjectMocks
    private CollectionSheetWritePlatformServiceJpaRepositoryImpl service;

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void bothCollectionSheetHandlersPreserveMakerAndDenyBeforePaymentsOrRepayments(boolean individual) {
        JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn("{}");
        var bulk = mock(CollectionSheetBulkDisbursalCommand.class);
        when(deserializer.commandFromApiJson("{}")).thenReturn(bulk);
        var context = new LoanCommandExecutionContext(mock(AppUser.class));
        doThrow(new GeneralPlatformDomainRuleException("denied", "Denied")).when(loans).validateBulkDisbursementAuthority(command, bulk,
                context);
        assertThrows(GeneralPlatformDomainRuleException.class, () -> {
            if (individual) {
                new SaveIndividualCollectionSheetCommandHandler(service).processLoanCommand(command, context);
            } else {
                new UpdateCollectionSheetCommandHandler(service).processLoanCommand(command, context);
            }
        });
        verify(loans).validateBulkDisbursementAuthority(command, bulk, context);
        verifyNoMoreInteractions(loans);
        verifyNoInteractions(payments);
    }
}
