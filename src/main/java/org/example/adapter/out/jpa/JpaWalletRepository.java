package org.example.adapter.out.jpa;

import org.example.domain.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;


public interface JpaWalletRepository extends JpaRepository<Wallet, UUID> {

    @Modifying
    @Query("""
              UPDATE Wallet w
                 SET w.balance = w.balance + :amount
               WHERE w.id = :id
            """)
    int depositAtomic(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("""
              UPDATE Wallet w
                 SET w.balance = w.balance - :amount
               WHERE w.id = :id
                 AND w.balance >= :amount
            """)
    int withdrawAtomic(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}