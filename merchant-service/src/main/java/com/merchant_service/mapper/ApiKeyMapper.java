package com.merchant_service.mapper;

import com.merchant_service.dto.response.ApiKeyCreateResponse;
import com.merchant_service.dto.response.ApiKeyResponse;
import com.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    /**
     * Maps the entity's stored {@code keySecretHash} into the response's {@code keySecret} property; this mapper does
     * not have the raw secret generated during initial key creation.
     */
    @Mapping(target = "keySecret", expression = "java(newSecretKey)")
    ApiKeyCreateResponse toApiKeyCreateResponse(ApiKey apiKey, String newSecretKey);

    /** Maps key metadata to list responses, whose DTO omits secret and hash fields. */
    List<ApiKeyResponse> toApikeyResponseList(List<ApiKey> apiKey);
}
