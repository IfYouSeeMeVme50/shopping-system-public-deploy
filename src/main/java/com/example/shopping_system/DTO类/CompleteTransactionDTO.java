package com.example.shopping_system.DTO类;

public class CompleteTransactionDTO {
    
    private Long intentId;
    private Boolean success;
    
    // Getters and Setters
    public Long getIntentId() { return intentId; }
    public void setIntentId(Long intentId) { this.intentId = intentId; }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
}
