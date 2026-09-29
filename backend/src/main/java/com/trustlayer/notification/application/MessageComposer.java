package com.trustlayer.notification.application;

import com.trustlayer.shared.event.DomainEvent;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class MessageComposer {

    record Message(String subject, String emailBody, String whatsappBody) {
    }

    private final String baseUrl;

    MessageComposer(@Value("${trustlayer.app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    Optional<Message> compose(DomainEvent event, String fullName) {
        String hello = "Hi " + fullName + ", ";
        return switch (event.eventType()) {
            case USER_REGISTERED -> Optional.of(new Message(
                    "Verify your email",
                    hello + "welcome to TrustLayer. Verify your email with this link: "
                            + baseUrl + "/verify-email?token=" + event.payload().get("verificationToken"),
                    hello + "welcome to TrustLayer. Please check your email to verify your address."));
            case PASSWORD_RESET_REQUESTED -> Optional.of(new Message(
                    "Reset your password",
                    hello + "reset your password with this link: "
                            + baseUrl + "/reset-password?token=" + event.payload().get("resetToken"),
                    null));
            case EMAIL_VERIFIED -> simple("Email verified", hello + "your email is verified. Next step is identity verification.");
            case IDENTITY_VERIFIED -> simple("Identity verified", hello + "your identity is verified. You can now choose a plan.");
            case IDENTITY_REJECTED -> simple("Identity verification failed", hello + "we could not verify your identity. You can start a new attempt.");
            case PAYMENT_SUCCEEDED -> simple("Payment received", hello + "your payment was received.");
            case PAYMENT_FAILED -> simple("Payment failed", hello + "your payment failed. Please check your payment method.");
            case SUBSCRIPTION_ACTIVATED -> simple("Subscription active", hello + "your subscription is now active.");
            case SUBSCRIPTION_CANCELLED -> simple("Subscription cancelled", hello + "your subscription was cancelled.");
            case ACCESS_GRANTED -> simple("Access granted", hello + "you now have full access.");
            case ACCESS_BLOCKED -> simple("Access blocked", hello + "your access has been blocked.");
        };
    }

    private Optional<Message> simple(String subject, String body) {
        return Optional.of(new Message(subject, body, body));
    }
}
