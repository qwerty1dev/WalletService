import org.example.domain.OperationType;
import org.example.domain.entity.Wallet;
import org.example.domain.repository.WalletRepository;
import org.example.usecase.interactor.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WalletServiceConcurrencyTest {

    private WalletRepository repository;
    private WalletServiceImpl service;
    private UUID walletId;

    @BeforeEach
    void setup() {
        repository = new InMemoryWalletRepository();
        walletId = UUID.randomUUID();
        repository.save(new Wallet(walletId, BigDecimal.ZERO));
        service = new WalletServiceImpl(repository);
    }

    @Test
    void testConcurrentDeposits() throws InterruptedException {
        int threads = 100;
        int perThread = 10;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            exec.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < perThread; j++) {
                        service.operate(walletId, OperationType.DEPOSIT, BigDecimal.ONE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        exec.shutdown();

        BigDecimal expected = BigDecimal.valueOf((long) threads * perThread);
        BigDecimal actual = service.getBalance(walletId);
        assertEquals(expected, actual, "Balance after concurrent deposits should match expected total");
    }


    static class InMemoryWalletRepository implements WalletRepository {
        private final ConcurrentMap<UUID, Wallet> store = new ConcurrentHashMap<>();

        @Override
        public Optional<Wallet> findById(UUID walletId) {
            return Optional.ofNullable(store.get(walletId));
        }

        @Override
        public Wallet save(Wallet wallet) {
            store.put(wallet.getId(), wallet);
            return wallet;
        }

        @Override
        public int depositAtomic(UUID walletId, BigDecimal amount) {

            return store.computeIfPresent(walletId, (id, w) -> {
                Wallet copy = new Wallet(w.getId(), w.getBalance().add(amount));
                return copy;
            }) != null ? 1 : 0;
        }

        @Override
        public int withdrawAtomic(UUID walletId, BigDecimal amount) {
            AtomicInteger result = new AtomicInteger(0);
            store.computeIfPresent(walletId, (id, w) -> {
                if (w.getBalance().compareTo(amount) >= 0) {
                    Wallet copy = new Wallet(w.getId(), w.getBalance().subtract(amount));
                    result.set(1);
                    return copy;
                } else {
                    result.set(0);
                    return w; // не меняем
                }
            });
            return result.get();
        }
    }
}