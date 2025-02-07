package com.reliaquest.api.util;

import java.util.UUID;
import org.dozer.DozerConverter;

public class UUIDConverter extends DozerConverter<UUID, UUID> {
    public UUIDConverter() {
        super(UUID.class, UUID.class);
    }

    public UUID convertTo(UUID source, UUID destination) {
        return source;
    }

    public UUID convertFrom(UUID source, UUID destination) {
        return source;
    }
}
