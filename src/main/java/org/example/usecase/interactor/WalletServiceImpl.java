package org.example.usecase.interactor;

import jakarta.transaction.Transactional;
import org.example.domain.OperationType;
import org.example.domain.entity.Wallet;
import org.example.domain.repository.WalletRepository;
import org.example.usecase.exception.InsufficientFundsException;
import org.example.usecase.exception.WalletNotFoundException;
import org.example.usecase.port.in.WalletService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;


@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository repo;

    public WalletServiceImpl(WalletRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void operate(UUID id, OperationType op, BigDecimal amount) {
        int updated;
        if (op == OperationType.DEPOSIT) {
            updated = repo.depositAtomic(id, amount);

            if (updated == 0) {
                Wallet w = new Wallet(id, amount);
                repo.save(w);
            }
        } else {

            updated = repo.withdrawAtomic(id, amount);
            if (updated == 0) {

                boolean exists = repo.findById(id).isPresent();
                if (!exists) {
                    throw new WalletNotFoundException(id);
                } else {
                    throw new InsufficientFundsException(id, amount);
                }
            }
        }
    }

    @Override
    public BigDecimal getBalance(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + id))
                .getBalance();
    }
}