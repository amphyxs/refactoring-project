package com.par.parapp.service;

import com.par.parapp.dto.ItemResponse;
import com.par.parapp.exception.ResourceNotFoundException;
import com.par.parapp.model.Game;
import com.par.parapp.model.Inventory;
import com.par.parapp.model.Item;
import com.par.parapp.model.User;
import com.par.parapp.repository.InventoryRepository;
import com.par.parapp.repository.ItemRepository;
import com.par.parapp.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    private final ItemRepository itemRepository;

    private final ShopRepository shopRepository;

    public InventoryService(InventoryRepository inventoryRepository, ItemRepository itemRepository,
            ShopRepository shopRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
        this.shopRepository = shopRepository;
    }

    public int saveInventory(User user, Game game) {
        List<Item> listOfItems = itemRepository.getAllItemsByGameId(game.getId())
                .orElseThrow(ResourceNotFoundException::new);
        Item commonItem = null;
        Item rareItem = null;
        Item legendaryItem = null;

        if (listOfItems != null) {
            for (Item item : listOfItems) {
                if (item != null) {
                    switch (item.getRarity()) {
                        case "Обычная" -> commonItem = item;
                        case "Редкая" -> rareItem = item;
                        case "Легендарная" -> legendaryItem = item;
                        default -> {
                        }
                    }
                }
            }
        }

        double rnd = Math.random();
        System.out.println(rnd);

        if (rnd > 0.80) {

            if (rnd > 0.95) {

                if (legendaryItem != null) {
                    inventoryRepository.save(new Inventory(user, legendaryItem, 0));
                    return 1;
                } else if (rareItem != null) {
                    inventoryRepository.save(new Inventory(user, rareItem, 0));
                    return 1;
                } else if (commonItem != null) {
                    inventoryRepository.save(new Inventory(user, commonItem, 0));
                    return 1;
                }

            } else if (rareItem != null) {
                inventoryRepository.save(new Inventory(user, rareItem, 0));
                return 1;
            } else if (commonItem != null) {
                inventoryRepository.save(new Inventory(user, commonItem, 0));
                return 1;
            }
        } else if (commonItem != null) {
            inventoryRepository.save(new Inventory(user, commonItem, 0));
            return 1;
        }
        return 0;
    }

    public Optional<Inventory> getByItemId(String userLogin, Long itemId) {
        Page<Inventory> allInventory = inventoryRepository.findAllByUser(userLogin, Pageable.unpaged())
                .orElseThrow(ResourceNotFoundException::new);
        List<Inventory> allInventoryData = allInventory.getContent();
        return allInventoryData.stream().filter(inventory -> {
            return inventory.getItem().getId().equals(itemId);
        }).findFirst();
    }

    public List<ItemResponse> getAllItems(String userLogin, int page, int size) {
        Pageable pageable = page != -1 ? PageRequest.of(page, size) : Pageable.unpaged();

        Page<Inventory> allInventory = inventoryRepository.findAllByUser(userLogin, pageable)
                .orElseThrow(ResourceNotFoundException::new);
        List<Inventory> allInventoryData = allInventory.getContent();
        List<ItemResponse> itemResponses = new ArrayList<>();
        allInventoryData.forEach(inventory -> {
            itemResponses.add(new ItemResponse(inventory.getItem().getItemUrl(),
                    inventory.getItem().getName(), inventory.getItem().getGame().getName(),
                    inventory.getItem().getRarity(), inventory.getAmount(),
                    shopRepository.getGameShopPictureByGameName(inventory.getItem().getGame().getName())));
        });

        return itemResponses;
    }

    public void saveItemToCustomer(User user, Item item) {
        Pageable pageable = Pageable.unpaged();
        Page<Inventory> allInventory = inventoryRepository.findAllByUser(user.getLogin(), pageable)
                .orElseThrow(ResourceNotFoundException::new);
        List<Inventory> allInventoryData = allInventory.getContent();

        allInventoryData.stream().filter(inventory -> inventory.getItem().getId().equals(item.getId())).findFirst()
                .ifPresentOrElse(inventory -> {
                    inventory.setAmount(inventory.getAmount() + 1);
                    inventoryRepository.save(inventory);
                }, () -> {
                    inventoryRepository.save(new Inventory(user, item, 1));
                });
    }

    public void removeItemFromSeller(User user, Item item) {
        Pageable pageable = Pageable.unpaged();
        Page<Inventory> allInventory = inventoryRepository.findAllByUser(user.getLogin(), pageable)
                .orElseThrow(ResourceNotFoundException::new);
        List<Inventory> allInventoryData = allInventory.getContent();

        allInventoryData.stream().filter(inventory -> inventory.getItem().getId().equals(item.getId())).findFirst()
                .ifPresent(inventory -> {
                    if (inventory.getAmount() == 1) {
                        inventoryRepository.delete(inventory);
                    } else {
                        inventory.setAmount(inventory.getAmount() - 1);
                        inventoryRepository.save(inventory);
                    }
                });
    }

}
