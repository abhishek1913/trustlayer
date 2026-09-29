package com.trustlayer.notification.domain;

public interface WhatsAppProvider {

    void send(String toPhoneNumber, String body);
}
