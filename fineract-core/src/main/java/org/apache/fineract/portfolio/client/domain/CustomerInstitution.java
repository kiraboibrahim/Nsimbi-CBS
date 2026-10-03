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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_customer_institution")
@Getter
@Setter
@NoArgsConstructor
public class CustomerInstitution extends AbstractPersistableCustom<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "institution_name", length = 150, nullable = false)
    private String institutionName;

    @Column(name = "registration_number", length = 100, nullable = false)
    private String registrationNumber;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "tin_number", length = 50, nullable = false)
    private String tinNumber;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "institution_category", length = 100)
    private String institutionCategory;

    @Column(name = "residence_ownership", length = 50)
    private String residenceOwnership;

    public CustomerInstitution(final Client client, final String institutionName, final String registrationNumber,
            final LocalDate registrationDate, final String tinNumber, final String businessType, final String institutionCategory,
            final String residenceOwnership) {
        this.client = client;
        this.institutionName = institutionName;
        this.registrationNumber = registrationNumber;
        this.registrationDate = registrationDate;
        this.tinNumber = tinNumber;
        this.businessType = businessType;
        this.institutionCategory = institutionCategory;
        this.residenceOwnership = residenceOwnership;
    }
}
