package com.payment_service.mapper;

import com.payment_service.dto.response.OrderResponse;
import com.payment_service.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    /** Maps order fields to the response and exposes the entity ID as {@code orderId} and merchant field as {@code merchantId}. */
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "merchantId", source = "merchant")     // TODO: Change column name to merchantId in OrderRecord
    OrderResponse toResponse(OrderRecord orderRecord);
}
