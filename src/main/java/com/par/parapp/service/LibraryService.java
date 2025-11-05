package com.par.parapp.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.par.parapp.dto.LastGamesResponse;
import com.par.parapp.dto.LibraryDataResponse;
import com.par.parapp.exception.BonusesForRefundWereSpentException;
import com.par.parapp.exception.ResourceNotFoundException;
import com.par.parapp.model.Game;
import com.par.parapp.model.Library;
import com.par.parapp.model.Shop;
import com.par.parapp.model.Transaction;
import com.par.parapp.model.User;
import com.par.parapp.repository.GameRepository;
import com.par.parapp.repository.LibraryRepository;
import com.par.parapp.repository.ShopRepository;
import com.par.parapp.repository.TransactionRepository;
import com.par.parapp.repository.UserRepository;
import com.par.parapp.repository.WalletRepository;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;

    private final ShopRepository shopRepository;

    private final GameRepository gameRepository;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private final WalletRepository walletRepository;

    private static final String SUCCESS_TRANSACTION_STATUS = "success";

    public LibraryService(LibraryRepository libraryRepository, ShopRepository shopRepository,
            GameRepository gameRepository, TransactionRepository transactionRepository, UserRepository userRepository,
            WalletRepository walletRepository) {
        this.libraryRepository = libraryRepository;
        this.shopRepository = shopRepository;
        this.gameRepository = gameRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public void saveGameInUserLibrary(User user, Game game) {
        Library library = new Library(user, game);
        libraryRepository.save(library);
    }

    public List<LibraryDataResponse> getEntriesByGameName(String gameName, String userLogin) {
        List<Library> allLibraryEntries;
        if (gameName == null)
            allLibraryEntries = libraryRepository.getAllFromLibraryByUserLogin(userLogin)
                    .orElseThrow(ResourceNotFoundException::new);
        else
            allLibraryEntries = libraryRepository.getAllFromLibraryByGameNameFilter(gameName, userLogin)
                    .orElseThrow(ResourceNotFoundException::new);

        List<LibraryDataResponse> libraryDataResponses = new ArrayList<>();

        allLibraryEntries.forEach(library -> {

            String gameNameToSend = library.getGame().getName();
            String gamePictureToSend = libraryRepository.getGameShopPictureByGameName(library.getGame().getName());
            String lastRunDateToSend;
            if (library.getLastRunDate() == null)
                lastRunDateToSend = "";
            else
                lastRunDateToSend = library.getLastRunDate().toString().substring(0,
                        library.getLastRunDate().toString().indexOf('.'));

            libraryDataResponses.add(new LibraryDataResponse(gameNameToSend,
                    gamePictureToSend,
                    lastRunDateToSend, library.getGame().getGameUrl()));

        });

        return libraryDataResponses;
    }

    public void enterTheGame(String login, String gameName) {
        libraryRepository.enterInGame(login, gameName);
    }

    public Integer getCountOfUserGames(String userLogin) {
        return libraryRepository.getCountOfGames(userLogin);
    }

    public List<LastGamesResponse> getUserLastGames(String userLogin) {
        List<Library> allLibraryEntries = libraryRepository.getAllFromLibraryByUserLogin(userLogin)
                .orElseThrow(ResourceNotFoundException::new);
        List<LastGamesResponse> lastGamesResponses = new ArrayList<>();
        allLibraryEntries.forEach(library -> {
            if (library.getLastRunDate() != null && lastGamesResponses.size() < 3) {
                lastGamesResponses.add(
                        new LastGamesResponse(shopRepository.getGameShopPictureByGameName(library.getGame().getName()),
                                library.getGame().getName(),
                                library.getLastRunDate().toString().substring(0,
                                        library.getLastRunDate().toString().indexOf('.'))));
            }
        });
        lastGamesResponses.sort(Comparator.comparing(LastGamesResponse::getLastEnterDate));
        Collections.reverse(lastGamesResponses);
        return lastGamesResponses;
    }

    public List<Game> getUserGames(String login, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Library> libraryList;
        Page<Library> libraryData = libraryRepository.findByUser(login, pageable)
                .orElseThrow(ResourceNotFoundException::new);
        List<Game> userGames = new ArrayList<>();
        libraryList = libraryData.getContent();
        libraryList.forEach(library -> userGames.add(library.getGame()));
        return userGames;
    }

    public void refundGame(String buyerLogin, String gameName) {
        var gameId = gameRepository.getGameByName(gameName)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"))
                .getId();
        var gameTransaction = transactionRepository.getSuccessTransactionForBuyingGameOfUser(buyerLogin, gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game transaction not found"));
        var bonusesTransaction = transactionRepository
                .getSuccessTransactionForBuyingGameWithBonusesOfUser(buyerLogin, gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonuses transaction not found"));
        var libraryItem = libraryRepository.getAllFromLibraryByGameNameFilter(gameName, buyerLogin)
                .orElseThrow(() -> new ResourceNotFoundException("Library item not found"))
                .get(0);
        var user = userRepository.getReferenceById(buyerLogin);

        if (bonusesTransaction.getAmount() > 0) {
            if (user.getWallet().getBonuses() < bonusesTransaction.getAmount()) {
                throw new BonusesForRefundWereSpentException();
            }

            user.getWallet().setBonuses(user.getWallet().getBonuses() - bonusesTransaction.getAmount());
            userRepository.replenishBalance(buyerLogin, gameTransaction.getAmount());
        } else {
            user.getWallet().setBonuses(user.getWallet().getBonuses() + -bonusesTransaction.getAmount());
            userRepository.replenishBalance(buyerLogin, gameTransaction.getAmount() + bonusesTransaction.getAmount());
        }
        walletRepository.save(user.getWallet());

        libraryRepository.delete(libraryItem);
        transactionRepository.delete(gameTransaction);
    }

    @Transactional
    public void transferMoneyAndBonuses(User buyer, Game game, Boolean isPayingWithBonuses) {
        Shop shopItem = shopRepository.getShopByGameName(game.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Shop item not found"));

        Double bonusesToUse;
        Double bonusesToAdd;
        if (Boolean.TRUE.equals(isPayingWithBonuses)) {
            bonusesToUse = Math.min(userRepository.getBonuses(buyer.getLogin()), shopItem.getPrice());
            bonusesToAdd = 0.0d;
            Transaction.TransactionParams params1 = new Transaction.TransactionParams();
            params1.setUser(buyer);
            params1.setPaymentMethod("bonuses");
            params1.setAmount(-bonusesToUse);
            params1.setTransactionDate(new Timestamp(System.currentTimeMillis()));
            params1.setTransactionStatus(SUCCESS_TRANSACTION_STATUS);
            params1.setItem(null);
            params1.setGame(game);
            params1.setWallet(null);
            transactionRepository.save(new Transaction(params1));
        } else {
            bonusesToUse = 0.0d;
            bonusesToAdd = shopItem.getPrice() * 0.05d;
            Transaction.TransactionParams params2 = new Transaction.TransactionParams();
            params2.setUser(buyer);
            params2.setPaymentMethod("bonuses");
            params2.setAmount(bonusesToAdd);
            params2.setTransactionDate(new Timestamp(System.currentTimeMillis()));
            params2.setTransactionStatus(SUCCESS_TRANSACTION_STATUS);
            params2.setItem(null);
            params2.setGame(game);
            params2.setWallet(null);
            transactionRepository.save(new Transaction(params2));
        }

        Double initialBalance = buyer.getWallet().getBalance();
        Double initialBonuses = buyer.getWallet().getBonuses();
        buyer.getWallet().setBonuses(initialBonuses - bonusesToUse);
        buyer.getWallet().setBalance(initialBalance + bonusesToUse - shopItem.getPrice());
        Transaction.TransactionParams params3 = new Transaction.TransactionParams();
        params3.setUser(buyer);
        params3.setPaymentMethod("balance");
        params3.setAmount(shopItem.getPrice());
        params3.setTransactionDate(new Timestamp(System.currentTimeMillis()));
        params3.setTransactionStatus(SUCCESS_TRANSACTION_STATUS);
        params3.setItem(null);
        params3.setGame(game);
        params3.setWallet(null);
        transactionRepository.save(new Transaction(params3));
        buyer.getWallet().setBonuses(buyer.getWallet().getBonuses() + bonusesToAdd);

        walletRepository.save(buyer.getWallet());
    }

}
