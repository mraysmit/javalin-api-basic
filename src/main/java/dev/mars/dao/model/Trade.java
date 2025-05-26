package dev.mars.dao.model;

import java.time.LocalDate;

/**
 * Represents a financial trade entity.
 */
public class Trade {
    private int id;
    private String symbol;
    private int quantity;
    private double price;
    private String type;
    private String status;
    private LocalDate tradeDate;
    private LocalDate settlementDate;
    private String counterparty;
    private String notes;

    // Default constructor for JSON deserialization
    public Trade() {
    }

    public Trade(int id, String symbol, int quantity, double price, String type,
                 String status, LocalDate tradeDate, LocalDate settlementDate,
                 String counterparty, String notes) {
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
        this.status = status;
        this.tradeDate = tradeDate;
        this.settlementDate = settlementDate;
        this.counterparty = counterparty;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}