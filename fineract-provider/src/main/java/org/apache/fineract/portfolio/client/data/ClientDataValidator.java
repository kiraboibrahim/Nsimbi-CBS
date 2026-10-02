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
package org.apache.fineract.portfolio.client.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.PhoneNumberValidationService;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecks;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.domain.CustomerType;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.validation.constraints.DateFormatValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ClientDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final EntityDatatableChecksRepository entityDatatableChecksRepository;
    private final PhoneNumberValidationService phoneNumberValidationService;

    @Autowired
    public ClientDataValidator(final FromJsonHelper fromApiJsonHelper,
            final ConfigurationReadPlatformService configurationReadPlatformService,
            final EntityDatatableChecksRepository entityDatatableChecksRepository,
            final PhoneNumberValidationService phoneNumberValidationService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.configurationReadPlatformService = configurationReadPlatformService;
        this.entityDatatableChecksRepository = entityDatatableChecksRepository;
        this.phoneNumberValidationService = phoneNumberValidationService;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.CLIENT_CREATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientNonPersonDetailsParamName, element)) {
            final String clientNonPersonJson = this.fromApiJsonHelper
                    .toJson(element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
            if (clientNonPersonJson != null) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, clientNonPersonJson,
                        ClientApiCollectionConstants.CLIENT_NON_PERSON_CREATE_REQUEST_DATA_PARAMETERS);
            }
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.officeIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.groupIdParamName, element)) {
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.groupIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.groupIdParamName).value(groupId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.staffIdParamName).value(staffId).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.accountNoParamName, element)) {
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.accountNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.savingsProductIdParamName, element)) {
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.savingsProductIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.savingsProductIdParamName).value(savingsProductId).ignoreIfNull()
                    .longGreaterThanZero();
            /*
             * if (savingsProductId != null && this.fromApiJsonHelper.parameterExists(ClientApiConstants. datatables,
             * element)) { final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(ClientApiConstants.
             * datatables, element); if (datatables.size() > 0) {
             * baseDataValidator.reset().parameter(ClientApiConstants.
             * savingsProductIdParamName).value(savingsProductId) .failWithCodeNoParameterAddedToErrorCode(
             * "should.not.be.used.with.datatables.parameter"); } }
             */
        }

        if (isFullnameProvided(element) || isIndividualNameProvided(element)) {

            // 1. No individual name part provided and fullname provided
            if (isFullnameProvided(element) && !isIndividualNameProvided(element)) {
                fullnameCannotBeBlank(element, baseDataValidator);
            }

            // 2. no fullname provided and individual name part provided
            if (isIndividualNameProvided(element) && !isFullnameProvided(element)) {
                validateRequiredIndividualNamePartsExist(element, baseDataValidator);
            }

            // 3. both provided
            if (isFullnameProvided(element) && isIndividualNameProvided(element)) {
                validateIndividualNamePartsCannotBeUsedWithFullname(element, baseDataValidator);
            }
        } else {

            if (isFullnameParameterPassed(element) || isIndividualNamePartParameterPassed(element)) {

                // 1. No individual name parameter passed and fullname passed
                if (isFullnameParameterPassed(element) && !isIndividualNamePartParameterPassed(element)) {
                    fullnameCannotBeBlank(element, baseDataValidator);
                }

                // 2. no fullname passed and individual name part passed
                if (isIndividualNamePartParameterPassed(element) && !isFullnameParameterPassed(element)) {
                    validateRequiredIndividualNamePartsExist(element, baseDataValidator);
                }

                // 3. both parameter types passed
                if (isFullnameParameterPassed(element) && isIndividualNamePartParameterPassed(element)) {
                    baseDataValidator.reset().parameter(ClientApiConstants.idParamName).failWithCode(".no.name.details.passed");
                }

            } else {
                baseDataValidator.reset().parameter(ClientApiConstants.idParamName).failWithCode(".no.name.details.passed");
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.externalIdParamName).value(externalId).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).ignoreIfNull()
                    .matchesRegularExpression(phoneNumberValidationService.getRegex()).notExceedingLengthOf(50);
        }

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(joinedDate).notNull();
                /*
                 * if(this.fromApiJsonHelper.parameterExists(ClientApiConstants. datatables,element)){
                 * baseDataValidator.reset().parameter(ClientApiConstants. activeParamName).value(active)
                 * .failWithCodeNoParameterAddedToErrorCode( "should.not.be.used.with.datatables.parameter"); }
                 */
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        LocalDate submittedOnDate = null;
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.submittedOnDateParamName, element)) {
            submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.submittedOnDateParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateOfBirthParamName, element)) {
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.dateOfBirthParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.dateOfBirthParamName).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getBusinessLocalDate()).validateDateBefore(submittedOnDate);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.genderIdParamName, element)) {
            final Integer genderId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.genderIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.genderIdParamName).value(genderId).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientTypeIdParamName, element)) {
            final Integer clientType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.clientTypeIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientTypeIdParamName).value(clientType).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientClassificationIdParamName, element)) {
            final Integer clientClassification = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(ClientApiConstants.clientClassificationIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientClassificationIdParamName).value(clientClassification)
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateFormatParamName, element)) {
            final String dateFormat = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.dateFormatParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.dateFormatParamName).value(dateFormat).notBlank()
                    .validDateTimeFormatPattern();
        }

        final Integer legalFormId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.legalFormIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.legalFormIdParamName).value(legalFormId).notNull().inMinMaxRange(1, 2);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.datatables, element)) {
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(ClientApiConstants.datatables, element);
            if (areDatatablesRequiredForLegalForm(legalFormId)) {
                baseDataValidator.reset().parameter(ClientApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
            }
        }

        if (this.fromApiJsonHelper.parameterExists("isStaff", element)) {
            final Boolean isStaffFlag = this.fromApiJsonHelper.extractBooleanNamed("isStaff", element);
            baseDataValidator.reset().parameter("isStaff").value(isStaffFlag).notNull();
        }

        if (this.configurationReadPlatformService.retrieveGlobalConfiguration(GlobalConfigurationConstants.ENABLE_ADDRESS).isEnabled()) {
            final JsonArray address = this.fromApiJsonHelper.extractJsonArrayNamed(ClientApiConstants.address, element);
            baseDataValidator.reset().parameter(ClientApiConstants.address).value(address).ignoreIfNull().jsonArrayNotEmpty();
        }

        validateDateFormatWhenPresent(element, dataValidationErrors);

        List<ApiParameterError> dataValidationErrorsForClientNonPerson = getDataValidationErrorsForCreateOnClientNonPerson(
                element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
        dataValidationErrors.addAll(dataValidationErrorsForClientNonPerson);

        validateCustomerCifDetails(element, dataValidationErrors);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    List<ApiParameterError> getDataValidationErrorsForCreateOnClientNonPerson(JsonElement element) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpNumberParamName, element)) {
            final String incorpNumber = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpNumberParamName).value(incorpNumber).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.remarksParamName, element)) {
            final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.remarksParamName).value(remarks).ignoreIfNull()
                    .notExceedingLengthOf(150);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpValidityTillParamName, element)) {
            final LocalDate incorpValidityTill = this.fromApiJsonHelper
                    .extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpValidityTillParamName).value(incorpValidityTill).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.constitutionIdParamName, element)) {
            final Integer constitution = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.constitutionIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.constitutionIdParamName).value(constitution).integerGreaterThanZero()
                    .notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mainBusinessLineIdParamName, element)) {
            final Integer mainBusinessLine = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(ClientApiConstants.mainBusinessLineIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mainBusinessLineIdParamName).value(mainBusinessLine)
                    .integerGreaterThanZero();
        }

        return dataValidationErrors;
    }

    private void validateIndividualNamePartsCannotBeUsedWithFullname(final JsonElement element,
            final DataValidatorBuilder baseDataValidator) {
        final String firstnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.firstnameParamName, element);
        if (StringUtils.isNotBlank(firstnameParam)) {
            final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.firstnameParamName, firstnameParam);
        }

        final String middlenameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.middlenameParamName, element);
        if (StringUtils.isNotBlank(middlenameParam)) {
            final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.middlenameParamName, middlenameParam);
        }

        final String lastnameParamName = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.lastnameParamName, element);
        if (StringUtils.isNotBlank(lastnameParamName)) {
            final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.lastnameParamName, lastnameParamName);
        }
    }

    private void validateRequiredIndividualNamePartsExist(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final String firstnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.firstnameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.firstnameParamName).value(firstnameParam).notBlank()
                .notExceedingLengthOf(50);

        final String middlenameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.middlenameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.middlenameParamName).value(middlenameParam).ignoreIfNull()
                .notExceedingLengthOf(50);

        final String lastnameParamName = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.lastnameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.lastnameParamName).value(lastnameParamName).notBlank()
                .notExceedingLengthOf(50);
    }

    private void fullnameCannotBeBlank(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam).notBlank().notExceedingLengthOf(160);
    }

    private boolean isIndividualNamePartParameterPassed(final JsonElement element) {
        return this.fromApiJsonHelper.parameterExists(ClientApiConstants.firstnameParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.middlenameParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.lastnameParamName, element);
    }

    private boolean isFullnameParameterPassed(final JsonElement element) {
        return this.fromApiJsonHelper.parameterExists(ClientApiConstants.fullnameParamName, element);
    }

    private boolean isIndividualNameProvided(final JsonElement element) {
        final String firstname = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.firstnameParamName, element);
        final String middlename = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.middlenameParamName, element);
        final String lastname = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.lastnameParamName, element);

        return StringUtils.isNotBlank(firstname) || StringUtils.isNotBlank(middlename) || StringUtils.isNotBlank(lastname);
    }

    private boolean isFullnameProvided(final JsonElement element) {
        final String fullname = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
        return StringUtils.isNotBlank(fullname);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.CLIENT_UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientNonPersonDetailsParamName, element)) {
            final String clientNonPersonJson = this.fromApiJsonHelper
                    .toJson(element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
            if (clientNonPersonJson != null) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, clientNonPersonJson,
                        ClientApiCollectionConstants.CLIENT_NON_PERSON_UPDATE_REQUEST_DATA_PARAMETERS);
            }
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.accountNoParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.accountNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (isFullnameProvided(element) || isIndividualNameProvided(element)) {

            // 1. No individual name part provided and fullname provided
            if (isFullnameProvided(element) && !isIndividualNameProvided(element)) {
                fullnameCannotBeBlank(element, baseDataValidator);
            }

            // 2. no fullname provided and individual name part provided
            if (isIndividualNameProvided(element) && !isFullnameProvided(element)) {
                validateRequiredIndividualNamePartsExist(element, baseDataValidator);
            }

            // 3. both provided
            if (isFullnameProvided(element) && isIndividualNameProvided(element)) {
                validateIndividualNamePartsCannotBeUsedWithFullname(element, baseDataValidator);
            }
        } else {

            if (isFullnameParameterPassed(element) || isIndividualNamePartParameterPassed(element)) {

                // 1. No individual name parameter passed and fullname passed
                if (isFullnameParameterPassed(element) && !isIndividualNamePartParameterPassed(element)) {
                    fullnameCannotBeBlank(element, baseDataValidator);
                }

                // 2. no fullname passed and individual name part passed
                if (isIndividualNamePartParameterPassed(element) && !isFullnameParameterPassed(element)) {
                    validateRequiredIndividualNamePartsExist(element, baseDataValidator);
                }

                // 3. both parameter types passed
                if (isFullnameParameterPassed(element) && isIndividualNamePartParameterPassed(element)) {
                    baseDataValidator.reset().parameter(ClientApiConstants.idParamName).failWithCode(".no.name.details.passed");
                }

            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.fullnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.lastnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.middlenameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.firstnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.externalIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).ignoreIfNull()
                    .matchesRegularExpression(phoneNumberValidationService.getRegex()).notExceedingLengthOf(50);
        }

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.activeParamName, element);
        if (active != null) {
            atLeastOneParameterPassedForUpdate = true;
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.staffIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.staffIdParamName).value(staffId).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.savingsProductIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.savingsProductIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.savingsProductIdParamName).value(savingsProductId).ignoreIfNull()
                    .longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.genderIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer genderId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.genderIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.genderIdParamName).value(genderId).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientTypeIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer clientType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.clientTypeIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientTypeIdParamName).value(clientType).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientClassificationIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer clientClassification = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(ClientApiConstants.clientClassificationIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientClassificationIdParamName).value(clientClassification)
                    .integerGreaterThanZero();
        }

        LocalDate submittedDate = null;
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.submittedOnDateParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            submittedDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.submittedOnDateParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.submittedOnDateParamName).value(submittedDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateOfBirthParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.dateOfBirthParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.dateOfBirthParamName).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getBusinessLocalDate()).validateDateBefore(submittedDate);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateFormatParamName, element)) {
            final String dateFormat = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.dateFormatParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.dateFormatParamName).value(dateFormat).notBlank()
                    .validDateTimeFormatPattern();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.legalFormIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer legalFormId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.legalFormIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.legalFormIdParamName).value(legalFormId).notNull().inMinMaxRange(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists("isStaff", element)) {
            final Boolean isStaffFlag = this.fromApiJsonHelper.extractBooleanNamed("isStaff", element);
            baseDataValidator.reset().parameter("isStaff").value(isStaffFlag).notNull();
        }

        validateDateFormatWhenPresent(element, dataValidationErrors);

        Map<String, Object> parameterUpdateStatusDetails = getParameterUpdateStatusAndDataValidationErrorsForUpdateOnClientNonPerson(
                element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
        boolean atLeastOneParameterPassedForClientNonPersonUpdate = (boolean) parameterUpdateStatusDetails.get("parameterUpdateStatus");

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerTypeParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.hasLoanLimitParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerIndividualParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerGroupParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerInstitutionParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerJointParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.nextOfKinParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.employmentIncomeParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.externalBanksParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            validateCustomerCifDetails(element, dataValidationErrors);
        }

        if (!atLeastOneParameterPassedForUpdate && !atLeastOneParameterPassedForClientNonPersonUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        @SuppressWarnings("unchecked")
        List<ApiParameterError> dataValidationErrorsForClientNonPerson = (List<ApiParameterError>) parameterUpdateStatusDetails
                .get("dataValidationErrors");
        dataValidationErrors.addAll(dataValidationErrorsForClientNonPerson);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    Map<String, Object> getParameterUpdateStatusAndDataValidationErrorsForUpdateOnClientNonPerson(JsonElement element) {
        boolean atLeastOneParameterPassedForUpdate = false;
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpNumberParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String incorpNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpNumberParamName).value(incorpNo).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.remarksParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.remarksParamName).value(remarks).notExceedingLengthOf(150);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpValidityTillParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate incorpValidityTill = this.fromApiJsonHelper
                    .extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpValidityTillParamName).value(incorpValidityTill);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.constitutionIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer constitutionId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.constitutionIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.constitutionIdParamName).value(constitutionId).integerGreaterThanZero()
                    .notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mainBusinessLineIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer mainBusinessLineId = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(ClientApiConstants.mainBusinessLineIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mainBusinessLineIdParamName).value(mainBusinessLineId)
                    .integerGreaterThanZero();
        }

        Map<String, Object> parameterUpdateStatusDetails = new HashMap<>();
        parameterUpdateStatusDetails.put("parameterUpdateStatus", atLeastOneParameterPassedForUpdate);
        parameterUpdateStatusDetails.put("dataValidationErrors", dataValidationErrors);

        return parameterUpdateStatusDetails;

    }

    public void validateActivation(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateDateFormatWhenPresent(final JsonElement element, final List<ApiParameterError> dataValidationErrors) {
        if (!this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateFormatParamName, element)) {
            return;
        }
        final String dateFormat = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.dateFormatParamName, element);
        if (StringUtils.isBlank(dateFormat)) {
            return;
        }
        if (!DateFormatValidator.isValidPattern(dateFormat)) {
            final String message = "Invalid dateFormat: `" + dateFormat + "`. Use a valid Java date/time pattern (e.g. dd MMMM yyyy).";
            dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.invalid.dateFormat.format", message,
                    ClientApiConstants.dateFormatParamName));
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateForUnassignStaff(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersUnassignStaff = new HashSet<>(Arrays.asList(ClientApiConstants.staffIdParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final String staffIdParameterName = ClientApiConstants.staffIdParamName;
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    public void validateForAssignStaff(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersUnassignStaff = new HashSet<>(Arrays.asList(ClientApiConstants.staffIdParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final String staffIdParameterName = ClientApiConstants.staffIdParamName;
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    public void validateClose(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.CLIENT_CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate closureDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.closureDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.closureDateParamName).value(closureDate).notNull();

        final Long closureReasonId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.closureReasonIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.closureReasonIdParamName).value(closureReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForSavingsAccount(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParameters = new HashSet<>(Arrays.asList(ClientApiConstants.savingsAccountIdParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final String savingsIdParameterName = ClientApiConstants.savingsAccountIdParamName;
        final Long savingsId = this.fromApiJsonHelper.extractLongNamed(savingsIdParameterName, element);
        baseDataValidator.reset().parameter(savingsIdParameterName).value(savingsId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    public void validateRejection(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiCollectionConstants.CLIENT_REJECT_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate rejectionDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.rejectionDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.rejectionDateParamName).value(rejectionDate).notNull();

        final Long rejectionReasonId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.rejectionReasonIdParamName, element);
        final String rejectionReasonText = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.rejectionReasonParamName, element);
        if (rejectionReasonId != null) {
            baseDataValidator.reset().parameter(ClientApiConstants.rejectionReasonIdParamName).value(rejectionReasonId).longGreaterThanZero();
        } else if (StringUtils.isNotBlank(rejectionReasonText)) {
            baseDataValidator.reset().parameter(ClientApiConstants.rejectionReasonParamName).value(rejectionReasonText).notBlank();
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.rejectionReasonIdParamName).value(rejectionReasonId).notNull()
                    .longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateWithdrawn(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiCollectionConstants.CLIENT_WITHDRAW_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate withdrawalDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.withdrawalDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.withdrawalDateParamName).value(withdrawalDate).notNull();

        final Long withdrawalReasonId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.withdrawalReasonIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.withdrawalReasonIdParamName).value(withdrawalReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateReactivate(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.REACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate reactivationDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.reactivationDateParamName,
                element);
        baseDataValidator.reset().parameter(ClientApiConstants.reactivationDateParamName).value(reactivationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateUndoRejection(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.UNDOREJECTION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate undoRejectionDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.reopenedDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.reopenedDateParamName).value(undoRejectionDate).notNull()
                .validateDateBeforeOrEqual(DateUtils.getBusinessLocalDate());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateUndoWithDrawn(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                ClientApiCollectionConstants.UNDOWITHDRAWN_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate undoWithdrawnDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.reopenedDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.reopenedDateParamName).value(undoWithdrawnDate).notNull()
                .validateDateBeforeOrEqual(DateUtils.getBusinessLocalDate());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    /**
     * Determines whether datatables are required for client creation based on configured entity datatable checks and
     * legal form.
     *
     * @param legalFormId
     *            the legal form ID (e.g. PERSON, ENTITY)
     * @return true if at least one required datatable is configured, false otherwise
     */
    private boolean areDatatablesRequiredForLegalForm(final Integer legalFormId) {
        final String entityName = EntityTables.CLIENT.getName();
        final Integer createStatus = StatusEnum.CREATE.getValue();
        final String entitySubtype = resolveEntitySubtype(legalFormId);

        final List<EntityDatatableChecks> requiredDatatables = entitySubtype == null
                ? entityDatatableChecksRepository.findByEntityAndStatus(entityName, createStatus)
                : entityDatatableChecksRepository.findByEntityAndStatusAndSubtype(entityName, createStatus, entitySubtype);

        return requiredDatatables != null && !requiredDatatables.isEmpty();
    }

    /**
     * Resolves the entity subtype label from the legal form ID. Returns null if the legal form is missing or invalid.
     */
    private String resolveEntitySubtype(final Integer legalFormId) {
        if (legalFormId == null) {
            return null;
        }
        final LegalForm legalForm = LegalForm.fromInt(legalFormId);
        return legalForm != null ? legalForm.getLabel().toUpperCase() : null;
    }

    private void validateCustomerCifDetails(final JsonElement element, final List<ApiParameterError> dataValidationErrors) {
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiCollectionConstants.CLIENT_RESOURCE_NAME);

        CustomerType customerType = null;
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerTypeParamName, element)) {
            final String customerTypeStr = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.customerTypeParamName, element);
            if (StringUtils.isNotBlank(customerTypeStr)) {
                customerType = CustomerType.fromString(customerTypeStr);
                if (customerType == null) {
                    baseDataValidator.reset().parameter(ClientApiConstants.customerTypeParamName).value(customerTypeStr)
                            .failWithCode("invalid.customer.type", "The customerType value '" + customerTypeStr + "' is invalid.");
                }
            }
        }

        // NIN validation: 14 alphanumeric characters per Uganda NIRA standard
        if (this.fromApiJsonHelper.parameterExists("nin", element)) {
            final String nin = this.fromApiJsonHelper.extractStringNamed("nin", element);
            if (StringUtils.isNotBlank(nin) && !nin.matches("^[A-Za-z0-9]{14}$")) {
                baseDataValidator.reset().parameter("nin").value(nin)
                        .failWithCode("invalid.nin.format", "NIN must be 14 alphanumeric characters");
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerIndividualParamName, element)) {
            final JsonElement indElement = element.getAsJsonObject().get(ClientApiConstants.customerIndividualParamName);
            if (indElement != null && indElement.isJsonObject()) {
                final JsonObject indObj = indElement.getAsJsonObject();
                if (indObj.has("nin") && !indObj.get("nin").isJsonNull()) {
                    final String nin = indObj.get("nin").getAsString();
                    if (StringUtils.isNotBlank(nin) && !nin.matches("^[A-Za-z0-9]{14}$")) {
                        baseDataValidator.reset().parameter("nin").value(nin)
                                .failWithCode("invalid.nin.format", "NIN must be 14 alphanumeric characters");
                    }
                }
                if (indObj.has("cardNumber") && !indObj.get("cardNumber").isJsonNull() && indObj.has("isNin")
                        && indObj.get("isNin").getAsBoolean()) {
                    final String cardNo = indObj.get("cardNumber").getAsString();
                    if (StringUtils.isNotBlank(cardNo) && !cardNo.matches("^[A-Za-z0-9]{14}$")) {
                        baseDataValidator.reset().parameter("cardNumber").value(cardNo)
                                .failWithCode("invalid.nin.format", "NIN must be 14 alphanumeric characters");
                    }
                }
            }
        }

        // Next of Kin validation
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.nextOfKinParamName, element)) {
            final JsonElement nokElement = element.getAsJsonObject().get(ClientApiConstants.nextOfKinParamName);
            if (nokElement != null && nokElement.isJsonArray()) {
                final JsonArray nokArray = nokElement.getAsJsonArray();
                BigDecimal totalAllocation = BigDecimal.ZERO;
                for (int i = 0; i < nokArray.size(); i++) {
                    final JsonElement item = nokArray.get(i);
                    if (item.isJsonObject()) {
                        final JsonObject nokObj = item.getAsJsonObject();
                        if (!nokObj.has("firstName") || StringUtils.isBlank(nokObj.get("firstName").getAsString())) {
                            baseDataValidator.reset().parameter(ClientApiConstants.nextOfKinParamName + "[" + i + "].firstName")
                                    .failWithCode("cannot.be.blank", "Next of kin first name cannot be blank");
                        }
                        if (!nokObj.has("secondName") || StringUtils.isBlank(nokObj.get("secondName").getAsString())) {
                            baseDataValidator.reset().parameter(ClientApiConstants.nextOfKinParamName + "[" + i + "].secondName")
                                    .failWithCode("cannot.be.blank", "Next of kin second name cannot be blank");
                        }
                        if (!nokObj.has("relationship") || StringUtils.isBlank(nokObj.get("relationship").getAsString())) {
                            baseDataValidator.reset().parameter(ClientApiConstants.nextOfKinParamName + "[" + i + "].relationship")
                                    .failWithCode("cannot.be.blank", "Next of kin relationship cannot be blank");
                        }
                        if (nokObj.has("allocationPercentage") && !nokObj.get("allocationPercentage").isJsonNull()) {
                            final BigDecimal alloc = nokObj.get("allocationPercentage").getAsBigDecimal();
                            if (alloc.compareTo(BigDecimal.ZERO) < 0) {
                                baseDataValidator.reset().parameter(ClientApiConstants.nextOfKinParamName + "[" + i + "].allocationPercentage")
                                        .failWithCode("cannot.be.negative", "Allocation percentage cannot be negative");
                            }
                            totalAllocation = totalAllocation.add(alloc);
                        }
                    }
                }
                if (totalAllocation.compareTo(new BigDecimal("100.00")) > 0) {
                    baseDataValidator.reset().parameter(ClientApiConstants.nextOfKinParamName + ".allocationPercentage")
                            .value(totalAllocation)
                            .failWithCode("total.allocation.exceeds.100", "Total next of kin allocation percentage cannot exceed 100%");
                }
            }
        }

        // Group KYC validation
        if (CustomerType.GROUP.equals(customerType)
                && this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerGroupParamName, element)) {
            final JsonElement groupElement = element.getAsJsonObject().get(ClientApiConstants.customerGroupParamName);
            if (groupElement != null && groupElement.isJsonObject()) {
                final JsonObject gObj = groupElement.getAsJsonObject();
                if (!gObj.has("groupName") || StringUtils.isBlank(gObj.get("groupName").getAsString())) {
                    baseDataValidator.reset().parameter("groupName").failWithCode("cannot.be.blank", "Group name is mandatory");
                }
                if (!gObj.has("registrationNumber") || StringUtils.isBlank(gObj.get("registrationNumber").getAsString())) {
                    baseDataValidator.reset().parameter("registrationNumber").failWithCode("cannot.be.blank",
                            "Registration number is mandatory");
                }
            }
        }

        // Institution KYC validation
        if (CustomerType.INSTITUTION.equals(customerType)
                && this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerInstitutionParamName, element)) {
            final JsonElement instElement = element.getAsJsonObject().get(ClientApiConstants.customerInstitutionParamName);
            if (instElement != null && instElement.isJsonObject()) {
                final JsonObject iObj = instElement.getAsJsonObject();
                if (!iObj.has("institutionName") || StringUtils.isBlank(iObj.get("institutionName").getAsString())) {
                    baseDataValidator.reset().parameter("institutionName").failWithCode("cannot.be.blank",
                            "Institution name is mandatory");
                }
                if (!iObj.has("tinNumber") || StringUtils.isBlank(iObj.get("tinNumber").getAsString())) {
                    baseDataValidator.reset().parameter("tinNumber").failWithCode("cannot.be.blank", "TIN number is mandatory");
                }
                if (!iObj.has("registrationNumber") || StringUtils.isBlank(iObj.get("registrationNumber").getAsString())) {
                    baseDataValidator.reset().parameter("registrationNumber").failWithCode("cannot.be.blank",
                            "Registration number is mandatory");
                }
            }
        }

        // Joint KYC validation
        if (CustomerType.JOINT.equals(customerType)
                && this.fromApiJsonHelper.parameterExists(ClientApiConstants.customerJointParamName, element)) {
            final JsonElement jointElement = element.getAsJsonObject().get(ClientApiConstants.customerJointParamName);
            if (jointElement != null && jointElement.isJsonObject()) {
                final JsonObject jObj = jointElement.getAsJsonObject();
                if (!jObj.has("jointName") || StringUtils.isBlank(jObj.get("jointName").getAsString())) {
                    baseDataValidator.reset().parameter("jointName").failWithCode("cannot.be.blank", "Joint name is mandatory");
                }
            }
        }
    }
}
