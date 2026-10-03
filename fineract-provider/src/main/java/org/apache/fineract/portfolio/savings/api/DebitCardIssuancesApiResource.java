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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.DebitCardIssuanceData;
import org.apache.fineract.portfolio.savings.service.DebitCardIssuanceReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/debitcards")
@Component
@Tag(name = "Debit Card Issuances", description = "Operations for requesting, approving, issuing, and rejecting co-branded debit cards")
@RequiredArgsConstructor
public class DebitCardIssuancesApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<DebitCardIssuanceData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DebitCardIssuanceReadPlatformService debitCardIssuanceReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Request debit card", description = "Initiates a new debit card requisition linked to an active savings account")
    public String request(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createDebitCardIssuance() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List debit card issuances", description = "Returns all debit card issuances filtered by status or savings account")
    public String retrieveAll(@QueryParam("status") final String status,
            @QueryParam("savingsAccountId") final Long savingsAccountId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission("DEBITCARDISSUANCE");

        final List<DebitCardIssuanceData> list = this.debitCardIssuanceReadPlatformService.retrieveAll(status, savingsAccountId);
        return this.toApiJsonSerializer.serialize(list);
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve debit card issuance", description = "Returns status and details of an individual debit card issuance")
    public String retrieveOne(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission("DEBITCARDISSUANCE");
        final DebitCardIssuanceData data = this.debitCardIssuanceReadPlatformService.retrieveOne(id);
        return this.toApiJsonSerializer.serialize(data);
    }

    @POST
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Process debit card commands", description = "Approve, issue, or reject debit card request")
    public String handleCommands(@PathParam("id") final Long id,
            @QueryParam("command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final String json = StringUtils.isNotBlank(apiRequestBodyAsJson) ? apiRequestBodyAsJson : "{}";

        if ("approve".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .approveDebitCardIssuance(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if ("issue".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .issueDebitCardIssuance(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if ("reject".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .rejectDebitCardIssuance(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        }

        throw new UnrecognizedQueryParamException("command", commandParam, "approve", "issue", "reject");
    }
}
