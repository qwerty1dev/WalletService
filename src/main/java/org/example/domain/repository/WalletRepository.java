package org.example.domain.repository;

import org.example.domain.entity.Wallet;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Optional<Wallet> findById(UUID walletId);
    Wallet save(Wallet wallet);

    int depositAtomic(UUID walletId, BigDecimal amount);
    int withdrawAtomic(UUID walletId, BigDecimal amount);
}
