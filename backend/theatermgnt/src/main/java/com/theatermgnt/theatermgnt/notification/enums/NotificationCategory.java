package com.theatermgnt.theatermgnt.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationCategory {
    BOOKING("Booking notifications - tickets, confirmations, cancellations"),
    SYSTEM("System notifications - maintenance, updates, announcements");

    private final String description;

    NotificationCategory(String description) {
        this.description = description;
    }
}
