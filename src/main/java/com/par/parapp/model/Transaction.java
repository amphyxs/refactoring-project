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

    public static class TransactionParams {
        private User user;
        private String paymentMethod;
        private Double amount;
        private Timestamp transactionDate;
        private String transactionStatus;
        private Item item;
        private Game game;
        private Wallet wallet;

        public void setUser(User user) { this.user = user; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public void setAmount(Double amount) { this.amount = amount; }
        public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }
        public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
        public void setItem(Item item) { this.item = item; }
        public void setGame(Game game) { this.game = game; }
        public void setWallet(Wallet wallet) { this.wallet = wallet; }

        public User getUser() { return user; }
        public String getPaymentMethod() { return paymentMethod; }
        public Double getAmount() { return amount; }
        public Timestamp getTransactionDate() { return transactionDate; }
        public String getTransactionStatus() { return transactionStatus; }
        public Item getItem() { return item; }
        public Game getGame() { return game; }
        public Wallet getWallet() { return wallet; }
    }

    public Transaction(TransactionParams params) {
    this.user = params.getUser();
    this.paymentMethod = params.getPaymentMethod();
    this.amount = params.getAmount();
    this.transactionDate = params.getTransactionDate();
    this.transactionStatus = params.getTransactionStatus();
    this.item = params.getItem();
    this.game = params.getGame();
    this.wallet = params.getWallet();
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