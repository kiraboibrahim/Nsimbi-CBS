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
package org.apache.fineract.useradministration.api;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.data.RoleOperatingHoursData;
import org.apache.fineract.useradministration.service.RoleOperatingHoursReadPlatformService;
import org.apache.fineract.useradministration.service.RoleOperatingHoursWritePlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/roles/{roleId}/operating-hours")
@Component
@Tag(name = "Role Operating Hours", description = "Defines and manages daily operational business hours for roles")
@RequiredArgsConstructor
public class RoleOperatingHoursApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "ROLEOPERATINGHOURS";

    private final PlatformSecurityContext context;
    private final RoleOperatingHoursReadPlatformService readPlatformService;
    private final RoleOperatingHoursWritePlatformService writePlatformService;
    private final DefaultToApiJsonSerializer<RoleOperatingHoursData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve role operating hours", description = "Retrieves weekly operating hours for a given role")
    public String retrieveAll(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<RoleOperatingHoursData> hours = this.readPlatformService.retrieveRoleOperatingHours(roleId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, hours);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update role operating hours", description = "Updates weekly operating hours for a given role")
    public String update(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.context.authenticatedUser().validateHasUpdatePermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final JsonElement parsed = this.fromJsonHelper.parse(apiRequestBodyAsJson);
        final JsonCommand command = JsonCommand.fromJsonElement(roleId, parsed, this.fromJsonHelper);
        final CommandProcessingResult result = this.writePlatformService.updateRoleOperatingHours(roleId, command);
        return this.toApiJsonSerializer.serialize(result);
    }
}
