package com.par.parapp.repository;

import com.par.parapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
        Optional<User> findByLogin(String login);

        Boolean existsByLogin(String login);

        Boolean existsByEmail(String email);

        @Transactional
        @Modifying
        @Query(value = "CALL user_login_update(:arg_login, 'active');", nativeQuery = true)
        void loginAsUser(@Param("arg_login") String login);

        @Transactional
        @Modifying
        @Query(value = "CALL user_login_update(:arg_login, 'inactive');", nativeQuery = true)
        void logoutFromUser(@Param("arg_login") String login);

        @Transactional
        @Modifying
        @Query(value = "CALL add_wallet_balance(:arg_login, :arg_balance);", nativeQuery = true)
        void replenishBalance(@Param("arg_login") String login, @Param("arg_balance") Double balance);

        @Transactional
        @Modifying
        @Query(value = "CALL add_wallet_balance(:arg_login, :arg_balance);", nativeQuery = true)
        void replenishBalanceSeller(@Param("arg_login") String login, @Param("arg_balance") Double balance);

        @Transactional
        @Modifying
        @Query(value = "CALL chargebalanceForSoldItem(:arg_login, :arg_balance, :arg_item_id);", nativeQuery = true)
        void chargeBalanceCustomer(@Param("arg_login") String login, @Param("arg_balance") Double balance,
                        @Param("arg_item_id") Long item_id);

        @Query(value = "SELECT balance FROM Wallets WHERE id = (SELECT wallet_id FROM Users WHERE login = :arg_login);", nativeQuery = true)
        Double getBalance(@Param("arg_login") String login);

        @Query(value = "SELECT bonuses FROM Wallets WHERE id = (SELECT wallet_id FROM Users WHERE login = :arg_login);", nativeQuery = true)
        Double getBonuses(@Param("arg_login") String login);

        @Transactional
        @Modifying
        @Query(value = "UPDATE users SET is_tutorial_completed = :is_tutorial_completed WHERE login = :user_login", nativeQuery = true)
        void updateIsTutorialCompleted(@Param("user_login") String userLogin,
                        @Param("is_tutorial_completed") Boolean isTutorialCompleted);
}
