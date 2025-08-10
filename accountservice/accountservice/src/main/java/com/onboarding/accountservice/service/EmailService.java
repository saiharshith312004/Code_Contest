package com.onboarding.accountservice.service;

import com.onboarding.accountservice.model.Customer;
import com.onboarding.accountservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final CustomerRepository customerRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, CustomerRepository customerRepository) {
        this.mailSender = mailSender;
        this.customerRepository = customerRepository;
    }

    public void sendKycRejectedEmail(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        String toEmail = customer.getEmail();
        String subject = "KYC Verification Failed";
        String body = "Dear " + customer.getFullName() + ",\n\n" +
                "Your eKYC has been failed due to discrepancies. " +
                "Please visit the nearest branch for assistance.\n\n" +
                "Regards,\nYour Bank Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
