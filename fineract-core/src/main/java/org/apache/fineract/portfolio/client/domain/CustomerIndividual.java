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
package org.apache.fineract.portfolio.client.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_customer_individual")
@Getter
@Setter
@NoArgsConstructor
public class CustomerIndividual extends AbstractPersistableCustom<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "salutation", length = 20)
    private String salutation;

    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    @Column(name = "is_dependent", nullable = false)
    private boolean isDependent;

    @Column(name = "is_pwd", nullable = false)
    private boolean isPwd;

    @Column(name = "country_of_birth", length = 100)
    private String countryOfBirth;

    @Column(name = "nationality", length = 100, nullable = false)
    private String nationality = "Ugandan";

    @Column(name = "home_ownership", length = 30)
    private String homeOwnership;

    @Column(name = "card_number", length = 100)
    private String cardNumber;

    public CustomerIndividual(final Client client, final String salutation, final String maritalStatus,
            final boolean isDependent, final boolean isPwd, final String countryOfBirth, final String nationality,
            final String homeOwnership, final String cardNumber) {
        this.client = client;
        this.salutation = salutation;
        this.maritalStatus = maritalStatus;
        this.isDependent = isDependent;
        this.isPwd = isPwd;
        this.countryOfBirth = countryOfBirth;
        if (nationality != null) {
            this.nationality = nationality;
        }
        this.homeOwnership = homeOwnership;
        this.cardNumber = cardNumber;
    }
}
