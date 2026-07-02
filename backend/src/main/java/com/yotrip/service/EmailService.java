package com.yotrip.service;

import com.yotrip.entity.Booking;
import com.yotrip.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingConfirmation(User user, Booking booking, String paymentId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("YoTrip - Booking Confirmed");
            message.setText(String.format(
                    "Hi %s,%n%nYour flight booking is confirmed.%nBooking ID: %s%nPayment ID: %s%nTotal: ₹%s%n%nThank you for booking with MakeMyTrip!",
                    user.getName(),
                    booking.getBookingId(),
                    paymentId,
                    booking.getTotalAmount()));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send confirmation email: {}", e.getMessage());
        }
    }
}
