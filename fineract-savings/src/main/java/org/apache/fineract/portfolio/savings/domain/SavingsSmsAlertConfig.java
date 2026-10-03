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
package org.apache.fineract.portfolio.savings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_savings_sms_alert_configs")
@Getter
@Setter
@NoArgsConstructor
public class SavingsSmsAlertConfig extends AbstractPersistableCustom<Long> {

    @OneToOne
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount savingsAccount;

    @Column(name = "notify_on_deposit", nullable = false)
    private boolean notifyOnDeposit = true;

    @Column(name = "notify_on_withdrawal", nullable = false)
    private boolean notifyOnWithdrawal = true;

    @Column(name = "notify_on_earning_interest", nullable = false)
    private boolean notifyOnEarningInterest = false;

    @Column(name = "notify_on_earning_dividends", nullable = false)
    private boolean notifyOnEarningDividends = false;

    @Column(name = "notify_on_outgoing_transfer", nullable = false)
    private boolean notifyOnOutgoingTransfer = true;

    @Column(name = "notify_on_incoming_transfer", nullable = false)
    private boolean notifyOnIncomingTransfer = true;

    @Column(name = "notify_on_direct_debit", nullable = false)
    private boolean notifyOnDirectDebit = true;

    @Column(name = "notify_on_direct_credit", nullable = false)
    private boolean notifyOnDirectCredit = true;

    @Column(name = "notify_on_standing_order", nullable = false)
    private boolean notifyOnStandingOrder = true;

    @Column(name = "notify_on_loan_application", nullable = false)
    private boolean notifyOnLoanApplication = false;

    @Column(name = "notify_on_loan_disbursement", nullable = false)
    private boolean notifyOnLoanDisbursement = true;

    @Column(name = "notify_on_loan_payment", nullable = false)
    private boolean notifyOnLoanPayment = true;

    @Column(name = "notify_on_loan_refund", nullable = false)
    private boolean notifyOnLoanRefund = false;

    @Column(name = "notify_on_hold", nullable = false)
    private boolean notifyOnHold = false;

    public SavingsSmsAlertConfig(final SavingsAccount savingsAccount) {
        this.savingsAccount = savingsAccount;
    }

    public static SavingsSmsAlertConfig createDefault(final SavingsAccount savingsAccount) {
        return new SavingsSmsAlertConfig(savingsAccount);
    }

    public org.apache.fineract.portfolio.savings.data.SavingsSmsAlertConfigData toData() {
        return new org.apache.fineract.portfolio.savings.data.SavingsSmsAlertConfigData(getId(),
                getSavingsAccount() != null ? getSavingsAccount().getId() : null, isNotifyOnDeposit(), isNotifyOnWithdrawal(),
                isNotifyOnEarningInterest(), isNotifyOnEarningDividends(), isNotifyOnOutgoingTransfer(), isNotifyOnIncomingTransfer(),
                isNotifyOnDirectDebit(), isNotifyOnDirectCredit(), isNotifyOnStandingOrder(), isNotifyOnLoanApplication(),
                isNotifyOnLoanDisbursement(), isNotifyOnLoanPayment(), isNotifyOnLoanRefund(), isNotifyOnHold());
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInBooleanParameterNamed("notifyOnDeposit", this.notifyOnDeposit)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnDeposit");
            actualChanges.put("notifyOnDeposit", newValue);
            this.notifyOnDeposit = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnWithdrawal", this.notifyOnWithdrawal)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnWithdrawal");
            actualChanges.put("notifyOnWithdrawal", newValue);
            this.notifyOnWithdrawal = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnEarningInterest", this.notifyOnEarningInterest)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnEarningInterest");
            actualChanges.put("notifyOnEarningInterest", newValue);
            this.notifyOnEarningInterest = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnEarningDividends", this.notifyOnEarningDividends)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnEarningDividends");
            actualChanges.put("notifyOnEarningDividends", newValue);
            this.notifyOnEarningDividends = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnOutgoingTransfer", this.notifyOnOutgoingTransfer)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnOutgoingTransfer");
            actualChanges.put("notifyOnOutgoingTransfer", newValue);
            this.notifyOnOutgoingTransfer = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnIncomingTransfer", this.notifyOnIncomingTransfer)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnIncomingTransfer");
            actualChanges.put("notifyOnIncomingTransfer", newValue);
            this.notifyOnIncomingTransfer = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnDirectDebit", this.notifyOnDirectDebit)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnDirectDebit");
            actualChanges.put("notifyOnDirectDebit", newValue);
            this.notifyOnDirectDebit = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnDirectCredit", this.notifyOnDirectCredit)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnDirectCredit");
            actualChanges.put("notifyOnDirectCredit", newValue);
            this.notifyOnDirectCredit = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnStandingOrder", this.notifyOnStandingOrder)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnStandingOrder");
            actualChanges.put("notifyOnStandingOrder", newValue);
            this.notifyOnStandingOrder = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnLoanApplication", this.notifyOnLoanApplication)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnLoanApplication");
            actualChanges.put("notifyOnLoanApplication", newValue);
            this.notifyOnLoanApplication = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnLoanDisbursement", this.notifyOnLoanDisbursement)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnLoanDisbursement");
            actualChanges.put("notifyOnLoanDisbursement", newValue);
            this.notifyOnLoanDisbursement = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnLoanPayment", this.notifyOnLoanPayment)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnLoanPayment");
            actualChanges.put("notifyOnLoanPayment", newValue);
            this.notifyOnLoanPayment = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnLoanRefund", this.notifyOnLoanRefund)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnLoanRefund");
            actualChanges.put("notifyOnLoanRefund", newValue);
            this.notifyOnLoanRefund = newValue;
        }
        if (command.isChangeInBooleanParameterNamed("notifyOnHold", this.notifyOnHold)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("notifyOnHold");
            actualChanges.put("notifyOnHold", newValue);
            this.notifyOnHold = newValue;
        }
    }
}
