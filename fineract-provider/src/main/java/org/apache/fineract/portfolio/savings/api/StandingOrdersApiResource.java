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
import org.apache.fineract.portfolio.savings.data.StandingOrderData;
import org.apache.fineract.portfolio.savings.service.StandingOrderReadPlatformService;
import org.apache.fineract.portfolio.savings.service.StandingOrderWritePlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/standingorders")
@Component
@Tag(name = "Standing Orders", description = "Operations for creating, approving, rejecting, and recurring execution of Standing Orders")
@RequiredArgsConstructor
public class StandingOrdersApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<StandingOrderData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final StandingOrderReadPlatformService standingOrderReadPlatformService;
    private final StandingOrderWritePlatformService standingOrderWritePlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create standing order", description = "Creates a recurring transfer standing order between two member savings accounts")
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createStandingOrder() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List standing orders", description = "Returns all standing orders filtered by status or source account")
    public String retrieveAll(@QueryParam("status") final String status,
            @QueryParam("sourceAccountId") final Long sourceAccountId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission("STANDINGORDER");

        final List<StandingOrderData> list = this.standingOrderReadPlatformService.retrieveAll(status, sourceAccountId);
        return this.toApiJsonSerializer.serialize(list);
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve standing order", description = "Returns the configuration and status of an individual standing order")
    public String retrieveOne(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission("STANDINGORDER");
        final StandingOrderData data = this.standingOrderReadPlatformService.retrieveOne(id);
        return this.toApiJsonSerializer.serialize(data);
    }

    @POST
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Process standing order commands", description = "Approve, reject, or execute standing order")
    public String handleCommands(@PathParam("id") final Long id,
            @QueryParam("command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final String json = StringUtils.isNotBlank(apiRequestBodyAsJson) ? apiRequestBodyAsJson : "{}";

        if ("approve".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .approveStandingOrder(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if ("reject".equalsIgnoreCase(commandParam)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .rejectStandingOrder(id) //
                    .withJson(json) //
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if ("execute".equalsIgnoreCase(commandParam)) {
            this.context.authenticatedUser().validateHasReadPermission("STANDINGORDER");
            final CommandProcessingResult result = this.standingOrderWritePlatformService.executeStandingOrder(id);
            return this.toApiJsonSerializer.serialize(result);
        }

        throw new UnrecognizedQueryParamException("command", commandParam, "approve", "reject", "execute");
    }
}
