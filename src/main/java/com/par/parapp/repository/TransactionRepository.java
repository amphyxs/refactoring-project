package com.par.parapp.repository;

import org.springframework.stereotype.Repository;

import com.par.parapp.model.Transaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT * FROM transactions WHERE transactions.user_login=:user_login AND transactions.game_id=:game_id AND transaction_status = 'success' AND payment_method='balance' ORDER BY transactions.transaction_date DESC LIMIT 1", nativeQuery = true)
    Optional<Transaction> getSuccessTransactionForBuyingGameOfUser(@Param("user_login") String login,
            @Param("game_id") Long gameId);

    @Query(value = "SELECT * FROM transactions WHERE transactions.user_login=:user_login AND transactions.game_id=:game_id AND transaction_status = 'success' AND payment_method='bonuses' ORDER BY transactions.transaction_date DESC LIMIT 1", nativeQuery = true)
    Optional<Transaction> getSuccessTransactionForBuyingGameWithBonusesOfUser(@Param("user_login") String login,
            @Param("game_id") Long gameId);
}
