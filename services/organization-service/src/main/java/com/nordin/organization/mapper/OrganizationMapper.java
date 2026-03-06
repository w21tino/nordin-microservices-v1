package com.nordin.organization.mapper;

import com.nordin.organization.dto.DepartmentResponse;
import com.nordin.organization.dto.OrganizationRequest;
import com.nordin.organization.dto.OrganizationResponse;
import com.nordin.organization.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrganizationMapper {

    Organization toEntity(OrganizationRequest request);

    @Mapping(target = "departments", expression = "java(departments)")
    @Mapping(target = "message", expression = "java(message)")
    OrganizationResponse toResponse(Organization organization,
                                    List<DepartmentResponse> departments,
                                    String message);
}
