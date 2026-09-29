package com.trustlayer.payment.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record GatewayEvent(String id, String type, JsonNode object) {
}
