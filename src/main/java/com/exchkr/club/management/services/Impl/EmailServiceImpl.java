package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.services.EmailService;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.File;
import java.math.BigDecimal;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender; 

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInitialCredentials(String toEmail, String initialPassword, String messageText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@exchkr.com"); 
            message.setTo(toEmail);
            
            StringBuilder body = new StringBuilder();
            
            if (initialPassword != null) {
                // Scenario: New User Account Created
                message.setSubject("Welcome to Exchkr - Your Account Credentials");
                body.append("Hello,\n\n")
                    .append("A new account has been created for you.\n\n")
                    .append("Username: ").append(toEmail).append("\n")
                    .append("Password: ").append(initialPassword).append("\n\n")
                    .append("Please log in and change your password immediately for security.\n");
            } else {
                // Scenario: Existing User added to a new Club
                message.setSubject("Club Access Granted");
                body.append("Hello,\n\n")
                    .append("You have been granted access to a new club on Exchkr.\n\n")
                    .append("You can log in using your existing email and password to view the club dashboard.\n");
            }
            
            body.append("\nBest regards,\nThe Exchkr Team");

            message.setText(body.toString());
            mailSender.send(message);
            
            logger.info("Successfully sent email notification to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    @Override
    public void sendInvoiceEmail(String toEmail, String memberName, String invoiceTitle, String filePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true parameter indicates multipart message for attachments
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("no-reply@exchkr.com");
            helper.setTo(toEmail);
            helper.setSubject("New Invoice: " + invoiceTitle);

            String body = "Hello " + memberName + ",\n\n" +
                          "A new invoice has been generated for: " + invoiceTitle + ".\n" +
                          "Please find the attached PDF for full details and payment instructions.\n\n" +
                          "Best regards,\nThe Exchkr Team";

            helper.setText(body);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment("Invoice_" + invoiceTitle.replaceAll("\\s+", "_") + ".pdf", file);

            mailSender.send(message);
            logger.info("Invoice email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send invoice email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    
    @Override
    public void sendDueReminderEmail(String toEmail, String memberName, String dueTitle, String remainingAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@exchkr.com");
            message.setTo(toEmail);
            message.setSubject("Reminder: Unpaid Due - " + dueTitle);

            String body = String.format(
                "Hello %s,\n\n" +
                "This is a friendly reminder that you have an outstanding balance for: %s.\n\n" +
                "Remaining Amount: $%s\n\n" +
                "Please log in to your Exchkr dashboard to view details and complete the payment.\n\n" +
                "Best regards,\nThe Exchkr Team",
                memberName, dueTitle, remainingAmount
            );

            message.setText(body);
            mailSender.send(message);
            
            logger.info("Due reminder email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send due reminder email to {}: {}", toEmail, e.getMessage());
            // We don't want to crash the business logic if mail fails, 
            // but in a production app, you might re-throw a custom exception here.
        }
    }


    @Override
    @Async
    public void sendReimbursementRejectionEmail(
            String toEmail,
            String memberName,
            String reimbursementCategory,
            BigDecimal amount,
            String rejectReason
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@exchkr.com");
            message.setTo(toEmail);
            message.setSubject("Reimbursement Request Rejected");

            String body = String.format(
                    "Hello %s,\n\n" +
                            "We regret to inform you that your reimbursement request has been rejected.\n\n" +
                            "Reimbursement Category: %s\n" +
                            "Requested Amount: %s\n\n" +
                            "Reason for Rejection:\n" +
                            "%s\n\n" +
                            "If you believe this decision was made in error or need further clarification, " +
                            "please reach out to your club administrator.\n\n" +
                            "Best regards,\n" +
                            "The Exchkr Team",
                    memberName,
                    reimbursementCategory,
                    amount.toPlainString(),
                    rejectReason
            );

            message.setText(body);
            mailSender.send(message);

            logger.info("Reimbursement rejection email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error(
                    "Failed to send reimbursement rejection email to {}: {}",
                    toEmail,
                    e.getMessage()
            );
            // Not throwing exception to avoid breaking business flow
        }
    }

    @Override
    @Async
    public void sendReimbursementApproveEmail(
            String toEmail,
            String memberName,
            String reimbursementCategory,
            BigDecimal amount
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@exchkr.com");
            message.setTo(toEmail);
            message.setSubject("Reimbursement Request Approved");

            String body = String.format(
                    "Hello %s,\n\n" +
                            "Good news! Your reimbursement request has been approved.\n\n" +
                            "Reimbursement Category: %s\n" +
                            "Approved Amount: %s\n\n" +
                            "The amount will be processed shortly. If you have any questions, " +
                            "please reach out to your club administrator.\n\n" +
                            "Best regards,\n" +
                            "The Exchkr Team",
                    memberName,
                    reimbursementCategory,
                    amount.toPlainString()
            );

            message.setText(body);
            mailSender.send(message);

            logger.info("Reimbursement approval email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error(
                    "Failed to send reimbursement approval email to {}: {}",
                    toEmail,
                    e.getMessage()
            );
            // Not throwing exception to avoid breaking business flow
        }
    }

    @Override
    @Async("bulkMemberExecutor")
    public void sendMembersCredentials(String toEmail, String initialPassword, String messageText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@exchkr.com");
            message.setTo(toEmail);

            StringBuilder body = new StringBuilder();

            if (initialPassword != null) {
                // Scenario: New User Account Created
                message.setSubject("Welcome to Exchkr - Your Account Credentials");
                body.append("Hello,\n\n")
                        .append("A new account has been created for you.\n\n")
                        .append("Username: ").append(toEmail).append("\n")
                        .append("Password: ").append(initialPassword).append("\n\n")
                        .append("Please log in and change your password immediately for security.\n");
            } else {
                // Scenario: Existing User added to a new Club
                message.setSubject("Club Access Granted");
                body.append("Hello,\n\n")
                        .append("You have been granted access to a new club on Exchkr.\n\n")
                        .append("You can log in using your existing email and password to view the club dashboard.\n");
            }

            body.append("\nBest regards,\nThe Exchkr Team");

            message.setText(body.toString());
            mailSender.send(message);

            logger.info("Successfully sent email notification to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}