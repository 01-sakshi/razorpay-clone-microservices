package com.merchant_service.mapper;

import com.merchant_service.dto.response.WebhookConfigResponse;
import com.merchant_service.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookConfigMapper {

    /** Maps configuration metadata and the supplied raw secret; callers pass {@code null} on reads to avoid exposing it. */
    @Mapping(target = "webhookSecret", source = "rawSecret")
    WebhookConfigResponse toResponse(MerchantWebhookConfig merchantWebhookConfig, String rawSecret);
}
