package com.trustlayer.notification.domain;

public interface EmailProvider {

    void send(String to, String subject, String body);
}
