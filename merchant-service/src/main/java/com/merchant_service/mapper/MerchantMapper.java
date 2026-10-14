package com.merchant_service.mapper;

import com.merchant_service.dto.request.MerchantSignupRequest;
import com.merchant_service.dto.response.MerchantResponse;
import com.merchant_service.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    /** Maps the persisted merchant profile to the public DTO, explicitly exposing entity {@code status} as {@code merchantStatus}. */
    @Mapping(target = "merchantStatus", source = "status")
    MerchantResponse toResponse(Merchant merchant);

    /** Creates a merchant entity from signup fields; persistence-generated identity and defaults remain entity concerns. */
    Merchant toEntityFromSignUpRequest(MerchantSignupRequest merchantSignupRequest);
}
