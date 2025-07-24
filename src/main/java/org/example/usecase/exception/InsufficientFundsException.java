package org.example.usecase.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    private final UUID walletId;
    public InsufficientFundsException(UUID walletId, BigDecimal amount) {
        super("Insufficient funds: wallet=" + walletId + ", attempted withdrawal=" + amount);
        this.walletId = walletId;
    }
    public UUID getWalletId() { return walletId; }
}