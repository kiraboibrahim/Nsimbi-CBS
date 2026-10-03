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
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.FixedDepositData;
import org.apache.fineract.portfolio.savings.service.FixedDepositReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/fixeddeposits")
@Component
@Tag(name = "Fixed Deposits", description = "Operations for booking, approving, rejecting, and terminating SACCO Fixed Term Deposits")
@RequiredArgsConstructor
public class FixedDepositsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<FixedDepositData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final FixedDepositReadPlatformService fixedDepositReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Book a new fixed deposit placement", description = "Submits a new term deposit booking with till or savings offset funding")
    public String createFixedDeposit(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createFixedDepositBooking() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List fixed deposits", description = "Lists fixed deposits with optional status and customer filters")
    public String retrieveAll(@QueryParam("status") final String status,
            @QueryParam("customerId") final Long customerId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission("FIXEDDEPOSIT");

        if (customerId != null) {
            final List<FixedDepositData> list = this.fixedDepositReadPlatformService.retrieveByCustomerId(customerId);
            return this.toApiJsonSerializer.serialize(list);
        }

        final SearchParameters searchParameters = SearchParameters.builder() //
                .status(status) //
                .build();

        final Page<FixedDepositData> page = this.fixedDepositReadPlatformService.retrieveAll(searchParameters);
        return this.toApiJsonSerializer.serialize(page);
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve fixed deposit detail", description = "Returns the contract terms and status of an individual fixed deposit")
    public String retrieveOne(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission("FIXEDDEPOSIT");
        final FixedDepositData data = this.fixedDepositReadPlatformService.retrieveOne(id);
        return this.toApiJsonSerializer.serialize(data);
    }

    @POST
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Process fixed deposit commands", description = "Approve, reject, or terminate fixed deposit")
    public String handleCommands(@PathParam("id") final Long id,
            @QueryParam("command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final String json = StringUtils.isNotBlank(apiRequestBodyAsJson) ? apiRequestBodyAsJson : "{}";

        if ("approve".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .approveFixedDepositBooking(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if ("reject".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .rejectFixedDepositBooking(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if ("terminate".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .terminateFixedDepositBooking(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        }

        throw new UnrecognizedQueryParamException("command", commandParam, "approve", "reject", "terminate");
    }
}
