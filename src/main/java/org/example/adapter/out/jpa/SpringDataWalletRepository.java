package org.example.adapter.out.jpa;


import org.example.domain.entity.Wallet;
import org.example.domain.repository.WalletRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;


@Repository
public class SpringDataWalletRepository implements WalletRepository {

    private final JpaWalletRepository jpa;

    public SpringDataWalletRepository(JpaWalletRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Wallet> findById(UUID walletId) {
        return jpa.findById(walletId);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return jpa.save(wallet);
    }

    @Override
    @Transactional
    public int depositAtomic(UUID walletId, BigDecimal amount) {
        return jpa.depositAtomic(walletId, amount);
    }

    @Override
    @Transactional
    public int withdrawAtomic(UUID walletId, BigDecimal amount) {
        return jpa.withdrawAtomic(walletId, amount);
    }
}