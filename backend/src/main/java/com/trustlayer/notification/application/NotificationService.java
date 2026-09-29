package com.trustlayer.notification.application;

import com.trustlayer.notification.domain.Channel;
import com.trustlayer.notification.domain.EmailProvider;
import com.trustlayer.notification.domain.NotificationDelivery;
import com.trustlayer.notification.domain.WhatsAppProvider;
import com.trustlayer.shared.event.DomainEvent;
import com.trustlayer.user.api.UserDirectory;
import com.trustlayer.user.api.UserSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final UserDirectory users;
    private final MessageComposer composer;
    private final DeliveryRecorder recorder;
    private final DeliveryDispatcher dispatcher;
    private final EmailProvider email;
    private final WhatsAppProvider whatsapp;

    public NotificationService(UserDirectory users, MessageComposer composer, DeliveryRecorder recorder,
                               DeliveryDispatcher dispatcher, EmailProvider email, WhatsAppProvider whatsapp) {
        this.users = users;
        this.composer = composer;
        this.recorder = recorder;
        this.dispatcher = dispatcher;
        this.email = email;
        this.whatsapp = whatsapp;
    }

    public void handle(DomainEvent event) {
        try {
            Optional<UserSummary> user = users.findById(event.userId());
            if (user.isEmpty()) {
                return;
            }
            composer.compose(event, user.get().fullName()).ifPresent(message -> send(event, user.get(), message));
        } catch (RuntimeException e) {
            log.error("Notification handling failed for {}: {}", event, e.getClass().getSimpleName());
        }
    }

    private void send(DomainEvent event, UserSummary user, MessageComposer.Message message) {
        boolean whatsappPossible = message.whatsappBody() != null && user.phoneNumber() != null;
        List<Channel> channels = new ArrayList<>(List.of(Channel.EMAIL));
        if (whatsappPossible) {
            channels.add(Channel.WHATSAPP);
        }
        for (NotificationDelivery delivery : recorder.open(event, channels)) {
            if (delivery.getChannel() == Channel.EMAIL) {
                dispatcher.dispatch(delivery.getId(), () -> email.send(user.email(), message.subject(), message.emailBody()));
            } else {
                dispatcher.dispatch(delivery.getId(), () -> whatsapp.send(user.phoneNumber(), message.whatsappBody()));
            }
        }
    }
}
