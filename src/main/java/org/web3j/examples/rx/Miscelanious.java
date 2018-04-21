package org.web3j.examples.rx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import rx.Observable;
import rx.Subscription;
import static web3j.ethereum.client.userWeb3jInstance.UserWeb3jInstanceApplication.COUNT;

/**
 * @author I.T.W764
 */
public class Miscelanious {

  Web3j web3j;

  public Miscelanious() {
    
  }

  public void listenForBlock() {//does not work with infura address
    try {
      Subscription subscription = web3j.blockObservable(false).subscribe(block -> {
        System.out.println("Sweet, block number " + block.getBlock().getNumber() + " has just been created");
      }, Throwable::printStackTrace);

      try {
        TimeUnit.MINUTES.sleep(2);
      } catch (InterruptedException ex) {
      }
      subscription.unsubscribe();
    } catch (Exception e) {
      System.out.println("ERROR " + e);
    }
  }

  public void showLastTransactions() {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    System.out.println("Waiting for " + COUNT + " transactions...");
    Subscription subscription = web3j.blockObservable(true)
            .take(COUNT)
            .subscribe(ethBlock -> {
              EthBlock.Block block = ethBlock.getBlock();
              LocalDateTime timestamp = Instant.ofEpochSecond(
                      block.getTimestamp().longValueExact()).atZone(ZoneId.of("UTC")).toLocalDateTime();
              int transactionCount = block.getTransactions().size();
              String hash = block.getHash();
              String parentHash = block.getParentHash();

              System.out.println(
                      timestamp + " "
                      + "Tx count: " + transactionCount + ", "
                      + "Hash: " + hash + ", "
                      + "Parent hash: " + parentHash
              );
              countDownLatch.countDown();
            }, Throwable::printStackTrace);

    subscription.unsubscribe();

  }

  void countingEtherExample() throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    System.out.println("Waiting for " + COUNT + " transactions...");
    Observable<BigInteger> transactionValue = web3j.transactionObservable()
            .take(COUNT)
            .map(Transaction::getValue)
            .reduce(BigInteger.ZERO, BigInteger::add);

    Subscription subscription = transactionValue.subscribe(total -> {
      BigDecimal value = new BigDecimal(total);
      System.out.println("Transaction value: "
              + Convert.fromWei(value, Convert.Unit.ETHER) + " Ether (" + value + " Wei)");
      countDownLatch.countDown();
    }, Throwable::printStackTrace);

    countDownLatch.await(10, TimeUnit.MINUTES);
    subscription.unsubscribe();
  }
}
