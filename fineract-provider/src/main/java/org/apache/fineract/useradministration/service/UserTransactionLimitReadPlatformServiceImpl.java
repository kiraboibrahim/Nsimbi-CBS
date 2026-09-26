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
package org.apache.fineract.useradministration.service;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.data.UserTransactionLimitData;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.UserTransactionLimit;
import org.apache.fineract.useradministration.domain.UserTransactionLimitRepository;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserTransactionLimitReadPlatformServiceImpl implements UserTransactionLimitReadPlatformService {

    private final PlatformSecurityContext context;
    private final AppUserRepository appUserRepository;
    private final UserTransactionLimitRepository userTransactionLimitRepository;

    @Override
    public Collection<UserTransactionLimitData> retrieveUserTransactionLimits(final Long userId) {
        this.context.authenticatedUser();
        if (!this.appUserRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        final List<UserTransactionLimit> limits = this.userTransactionLimitRepository.findByAppUserId(userId);
        return limits.stream()
                .map(limit -> UserTransactionLimitData.instance(limit.getId(), userId, limit.getLimitType().name(),
                        limit.getMinAmount(), limit.getMaxAmount()))
                .toList();
    }
}
