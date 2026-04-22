package com.exchkr.club.management.services;

import java.math.BigDecimal;

public interface EmailService {
	// method to sent signup credentials
    void sendInitialCredentials(String toEmail, String username, String initialPassword);
    
    // method for Invoice attachments
    void sendInvoiceEmail(String toEmail, String memberName, String invoiceTitle, String filePath);
    
    // method to sent due reminder
    void sendDueReminderEmail(String toEmail, String memberName, String dueTitle, String remainingAmount);

    // method to sent reimbursement rejection
    void sendReimbursementRejectionEmail(String toEmail, String memberName, String reimbursementCategory, BigDecimal amount, String rejectReason);

    // method to sent reimbursement approval
    void sendReimbursementApproveEmail(  String toEmail, String memberName, String reimbursementCategory, BigDecimal amount);

    // method to sent signup credentials for multiple members
    void sendMembersCredentials(String toEmail, String username, String initialPassword);
}