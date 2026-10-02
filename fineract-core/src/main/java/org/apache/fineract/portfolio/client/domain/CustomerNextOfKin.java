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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_customer_next_of_kin")
@Getter
@Setter
@NoArgsConstructor
public class CustomerNextOfKin extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "second_name", length = 100, nullable = false)
    private String secondName;

    @Column(name = "phone", length = 50, nullable = false)
    private String phone;

    @Column(name = "physical_address")
    private String physicalAddress;

    @Column(name = "relationship", length = 50, nullable = false)
    private String relationship;

    @Column(name = "allocation_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal allocationPercentage = BigDecimal.ZERO;

    public CustomerNextOfKin(final Client client, final String firstName, final String secondName, final String phone,
            final String physicalAddress, final String relationship, final BigDecimal allocationPercentage) {
        this.client = client;
        this.firstName = firstName;
        this.secondName = secondName;
        this.phone = phone;
        this.physicalAddress = physicalAddress;
        this.relationship = relationship;
        this.allocationPercentage = allocationPercentage != null ? allocationPercentage : BigDecimal.ZERO;
    }
}
