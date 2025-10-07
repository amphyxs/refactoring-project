package com.par.parapp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.par.parapp.dto.SlotsResponse;
import com.par.parapp.exception.ResourceNotFoundException;
import com.par.parapp.model.Item;
import com.par.parapp.model.Market;
import com.par.parapp.model.User;
import com.par.parapp.repository.InventoryRepository;
import com.par.parapp.repository.MarketRepository;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public void save(User user, Item item, Double price) {
        marketRepository.save(new Market(user, item, price));
    }

    public Market getMarketById(Long id) {
        return marketRepository.getMarketById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<SlotsResponse> getEntriesByGameNameAndItemName(String itemName, String gameName) {
        List<Market> marketList;
        if (itemName.isEmpty())
            marketList = marketRepository.getAllSlots().orElseThrow(ResourceNotFoundException::new);
        else
            marketList = marketRepository.getAllFromMarketByItemNameFilter(itemName)
                    .orElseThrow(ResourceNotFoundException::new);

        List<SlotsResponse> slotsResponses = new ArrayList<>();

        if (!gameName.isEmpty()) {
            marketList.forEach(market -> {
                if (market.getItem().getGame().getName().equals(gameName))
                    slotsResponses.add(new SlotsResponse(market.getItem().getName(),
                            market.getItem().getItemUrl(),
                            market.getItem().getGame().getName(),
                            market.getItem().getRarity(),
                            market.getPrice(), market.getId()));
            });
        } else
            marketList.forEach(market -> {
                slotsResponses.add(new SlotsResponse(market.getItem().getName(),
                        market.getItem().getItemUrl(),
                        market.getItem().getGame().getName(),
                        market.getItem().getRarity(),
                        market.getPrice(), market.getId()));
            });

        slotsResponses.sort(Comparator.comparing(SlotsResponse::getPrice));

        return slotsResponses;

    }

    public void deleteSlot(Long marketId) {
        marketRepository.deleteMarketById(marketId);
    }

}
