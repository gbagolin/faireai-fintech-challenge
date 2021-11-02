package com.github.gilbertotcc.fintech.challenge;

import lombok.With;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import io.vavr.collection.List;


@With(value = AccessLevel.PRIVATE)
public class BalanceHistory {

  private final Balance referenceBalance;
  private final SortedMap<LocalDate, Balance> balances = new TreeMap<>();
  private final LinkedList<Transaction> backWardTransactions = new LinkedList<>();
  private final LinkedList<Transaction> onWardTransactions = new LinkedList<>();

  public BalanceHistory(Balance referenceBalance) {
    this.referenceBalance = referenceBalance;
    balances.put(referenceBalance.getDate(), referenceBalance);
  }

  public BalanceHistory addTransaction(Transaction transaction) {
    if (transaction.getBookedDate().compareTo(this.referenceBalance.getDate()) < 0) {
      backWardTransactions.add(transaction);
    } else {
      onWardTransactions.add(transaction);
    }
    return this;
  }

  /**
   * For each onward transaction, add the balance to the map.
   * The amount is computed as the cumulative sum given the balance and the transaction.
   */
  private void computeOnWardBalances() {
    onWardTransactions.sort((t1, t2) -> t1.getBookedDate().compareTo(t2.getBookedDate()));
    BigDecimal tmpBalance = referenceBalance.getAmount();
    for (Transaction transaction : onWardTransactions) {
      tmpBalance = tmpBalance.add(transaction.getAmount());
      Balance newBalance = Balance.of(transaction.getBookedDate(), tmpBalance);
      balances.put(newBalance.getDate(), newBalance);
    }
  }
  /**
   * For each backward transaction, add the balance to the map.
   * The amount is computed as the cumulative subtraction given the balance and the transaction.
   */
  private void computeBackwardBalances() {
    backWardTransactions.sort((t1, t2) -> t2.getBookedDate().compareTo(t1.getBookedDate()));
    BigDecimal tmpBalance = referenceBalance.getAmount();
    for (Transaction transaction : backWardTransactions) {
      tmpBalance = tmpBalance.subtract(transaction.getAmount());
      Balance newBalance = Balance.of(transaction.getBookedDate(), tmpBalance);
      balances.put(newBalance.getDate(), newBalance);
    }
  }

  public List<Balance> getBalances() {
    computeBackwardBalances();
    computeOnWardBalances();
    return List.ofAll(balances.values());
  }

}
