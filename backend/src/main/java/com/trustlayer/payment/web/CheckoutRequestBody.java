package com.trustlayer.payment.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequestBody(@NotBlank @Size(max = 50) String planCode) {
}
