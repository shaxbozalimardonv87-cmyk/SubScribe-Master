package com.examplesubscribemaster.mapper;

import com.examplesubscribemaster.dto.subscription.SubscriptionResponse;
import com.examplesubscribemaster.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    SubscriptionResponse toResponse(Subscription entity);
}
