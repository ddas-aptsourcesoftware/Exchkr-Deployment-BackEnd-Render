package com.exchkr.club.management.model.api.request;

import java.math.BigDecimal;
import java.util.List;

public class CreateInvoiceRequest {
    private String invoiceTitle;
    private List<LineItemRequest> lineItems;
    private BigDecimal totalAmount; // NEW: Total amount including fees from frontend
    private String dueDate; // yyyy-MM-dd
    private String additionalNotes;
    private List<Long> selectedMemberIds;

    public CreateInvoiceRequest() {}

    public String getInvoiceTitle() { 
        return invoiceTitle; 
    }

    public void setInvoiceTitle(String invoiceTitle) { 
        this.invoiceTitle = invoiceTitle; 
    }

    public List<LineItemRequest> getLineItems() { 
        return lineItems; 
    }

    public void setLineItems(List<LineItemRequest> lineItems) { 
        this.lineItems = lineItems; 
    }

    public BigDecimal getTotalAmount() { 
        return totalAmount; 
    }

    public void setTotalAmount(BigDecimal totalAmount) { 
        this.totalAmount = totalAmount; 
    }

    public String getDueDate() { 
        return dueDate; 
    }

    public void setDueDate(String dueDate) { 
        this.dueDate = dueDate; 
    }

    public String getAdditionalNotes() { 
        return additionalNotes; 
    }

    public void setAdditionalNotes(String additionalNotes) { 
        this.additionalNotes = additionalNotes; 
    }

    public List<Long> getSelectedMemberIds() { 
        return selectedMemberIds; 
    }

    public void setSelectedMemberIds(List<Long> selectedMemberIds) { 
        this.selectedMemberIds = selectedMemberIds; 
    }

    public static class LineItemRequest {
        private String description;
        private BigDecimal amount;

        public LineItemRequest() {}

        public String getDescription() { 
            return description; 
        }

        public void setDescription(String description) { 
            this.description = description; 
        }

        public BigDecimal getAmount() { 
            return amount; 
        }

        public void setAmount(BigDecimal amount) { 
            this.amount = amount; 
        }
    }
}