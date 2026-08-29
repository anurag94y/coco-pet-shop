package org.example.backend.dealer;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.dealer.dto.CreateDealerRequest;
import org.example.backend.dealer.dto.DealerAddressHistoryResponse;
import org.example.backend.dealer.dto.DealerResponse;
import org.example.backend.dealer.dto.UpdateDealerRequest;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DealerService {

    private final DealerRepository dealerRepository;
    private final ShopRepository shopRepository;
    private final DealerAddressHistoryRepository addressHistoryRepository;

    public DealerService(
            DealerRepository dealerRepository,
            ShopRepository shopRepository,
            DealerAddressHistoryRepository addressHistoryRepository
    ) {
        this.dealerRepository = dealerRepository;
        this.shopRepository = shopRepository;
        this.addressHistoryRepository = addressHistoryRepository;
    }

    @Transactional
    public DealerResponse createDealer(CreateDealerRequest request) {

        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop not found: " + request.shopId()
                        )
                );

        Dealer dealer = new Dealer();
        dealer.setShop(shop);
        dealer.setName(request.name());
        dealer.setPhone(request.phone());
        dealer.setAddress(request.address());

        Dealer savedDealer = dealerRepository.save(dealer);

        if (request.address() != null && !request.address().isBlank()) {

            DealerAddressHistory history = new DealerAddressHistory();

            history.setDealer(savedDealer);
            history.setAddress(request.address());
            history.setValidFrom(LocalDateTime.now());

            addressHistoryRepository.save(history);
        }

        return toResponse(savedDealer);
    }

    @Transactional(readOnly = true)
    public DealerResponse getDealer(Long dealerId) {

        Dealer dealer = getDealerEntity(dealerId);

        return toResponse(dealer);
    }

    @Transactional(readOnly = true)
    public List<DealerResponse> getDealers(Long shopId) {

        return dealerRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DealerResponse updateDealer(
            Long dealerId,
            UpdateDealerRequest request
    ) {

        Dealer dealer = getDealerEntity(dealerId);

        if (!Objects.equals(dealer.getAddress(), request.address())) {
            updateAddressHistory(dealer, request.address());
        }

        dealer.setName(request.name());
        dealer.setPhone(request.phone());
        dealer.setAddress(request.address());

        return toResponse(dealer);
    }

    @Transactional(readOnly = true)
    public List<DealerAddressHistoryResponse> getAddressHistory(
            Long dealerId
    ) {

        getDealerEntity(dealerId);

        return addressHistoryRepository
                .findByDealerIdOrderByValidFromDesc(dealerId)
                .stream()
                .map(history ->
                        new DealerAddressHistoryResponse(
                                history.getId(),
                                history.getAddress(),
                                history.getValidFrom(),
                                history.getValidTo()
                        )
                )
                .toList();
    }

    private Dealer getDealerEntity(Long dealerId) {

        return dealerRepository.findById(dealerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Dealer not found: " + dealerId
                        )
                );
    }

    private DealerResponse toResponse(Dealer dealer) {

        return new DealerResponse(
                dealer.getId(),
                dealer.getShop().getId(),
                dealer.getName(),
                dealer.getPhone(),
                dealer.getAddress(),
                dealer.getCreatedAt(),
                dealer.getUpdatedAt()
        );
    }

    private void updateAddressHistory(
            Dealer dealer,
            String newAddress
    ) {

        LocalDateTime now = LocalDateTime.now();

        addressHistoryRepository
                .findFirstByDealerIdAndValidToIsNullOrderByValidFromDesc(
                        dealer.getId()
                )
                .ifPresent(currentAddress ->
                        currentAddress.setValidTo(now)
                );

        if (newAddress != null && !newAddress.isBlank()) {

            DealerAddressHistory newHistory =
                    new DealerAddressHistory();

            newHistory.setDealer(dealer);
            newHistory.setAddress(newAddress);
            newHistory.setValidFrom(now);

            addressHistoryRepository.save(newHistory);
        }
    }
}