package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;
import java.util.List;

public class MembersTransactionsResponse {
    private List<Transaction> transactions;

    public MembersTransactionsResponse(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public static class Transaction {
        private Long transId;
        private String category;
        private BigDecimal amount;
        private String status;
        private String  transDate;
        private String description;
        private Long dueId;

        public Transaction(Long transId, String category, BigDecimal amount,
                           String status, String  transDate, String description, Long dueId) {
            this.transId = transId;
            this.category = category;
            this.amount = amount;
            this.status = status;
            this.transDate = transDate;
            this.description = description;
            this.dueId = dueId;
        }

        public Long getTransId() {
            return transId;
        }

        public void setTransId(Long transId) {
            this.transId = transId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String  getTransDate() {
            return transDate;
        }

        public void setTransDate(String  transDate) {
            this.transDate = transDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getDueId() {
            return dueId;
        }

        public void setDueId(Long dueId) {
            this.dueId = dueId;
        }
    }
}
