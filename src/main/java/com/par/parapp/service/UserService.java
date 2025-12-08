package com.par.parapp.service;

import com.par.parapp.dto.UserDataResponse;
import com.par.parapp.exception.NotEnoughBalanceException;
import com.par.parapp.model.User;
import com.par.parapp.repository.TransactionRepository;
import com.par.parapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public UserService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public boolean checkUserOnExist(String login) {
        return userRepository.existsByLogin(login);
    }

    public User getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователя с логином " + login + " не существует"));
    }

    public String getUserStatus(String login) {
        return getUserByLogin(login).getStatus();
    }

    public UserDataResponse getUserLastLoginDate(String login) {

        String status = getUserStatus(login);

        var user = getUserByLogin(login);
        LocalDate lastLoginDate = user.getLastLoginDate();
        String lastLoginDateToSend;
        if (lastLoginDate == null)
            lastLoginDateToSend = "";
        else
            lastLoginDateToSend = lastLoginDate.toString();

        String registrationDate = getRegistrationDate(login).toString();
        return new UserDataResponse(status, lastLoginDateToSend, registrationDate, user.getIsTutorialCompleted());
    }

    public LocalDate getRegistrationDate(String login) {
        return getUserByLogin(login).getRegistrationDate();
    }

    public void logoutAsUser(String login) {
        userRepository.logoutFromUser(login);
    }

    public void addBalance(String login, Double balance) {
        userRepository.replenishBalance(login, balance);
    }

    public Double getBalance(String login) {
        return userRepository.getBalance(login);
    }

    public Double getBonuses(String login) {
        return userRepository.getBonuses(login);
    }

    public void replenishBalanceSeller(String userLogin, Double balance) {
        userRepository.replenishBalanceSeller(userLogin, balance);
    }

    @Transactional
    public void chargeBalanceCustomer(String userLogin, Double balance, Long itemId) {
        userRepository.chargeBalanceCustomer(userLogin, balance);
        transactionRepository.createTransactionForItemPurchase(userLogin, balance, itemId);
    }

    public void checkBalanceToBuyGame(User user, double marketPrice) {
        if (user.getWallet().getBalance() < marketPrice)
            throw new NotEnoughBalanceException("У вас недостаточно средств для покупки этой вещи!");
    }

    public void updateIsTutorialCompleted(String userLogin, Boolean isTutorialCompleted) {
        userRepository.updateIsTutorialCompleted(userLogin, isTutorialCompleted);
    }

}