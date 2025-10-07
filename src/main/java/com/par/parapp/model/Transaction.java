package com.par.parapp.model;

import java.sql.Timestamp;

import javax.persistence.*;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "user_login", referencedColumnName = "login")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "payment_method")
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Column(name = "amount")
    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Column(name = "transaction_date", insertable = false, updatable = false)
    private Timestamp transactionDate;

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Column(name = "transaction_status")
    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @ManyToOne
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private Wallet wallet;

    public Transaction(User user, String paymentMethod, Double amount, Timestamp transactionDate,
            String transactionStatus, Item item, Game game, Wallet wallet) {
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.transactionStatus = transactionStatus;
        this.item = item;
        this.game = game;
        this.wallet = wallet;
    }

    public Transaction() {
        super();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}