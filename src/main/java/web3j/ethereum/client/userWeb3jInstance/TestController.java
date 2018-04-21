package web3j.ethereum.client.userWeb3jInstance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rx.Observable;
import rx.Subscription;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

/**
 * Demonstrations of working with RxJava's Observables in web3j.
 */
@Controller
//@RequestMapping("/client")
public class TestController {

  private static final int COUNT = 10;

//  private static final Logger log = LoggerFactory.getLogger(TestController.class);
  @Autowired
  private Web3j web3j;
//  private final Web3j web3j;

  public TestController() {
//    web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/eIwwjgj5xmZFQyh0450I"));  // defaults to http://localhost:8545/
//    web3j = Web3j.build(new HttpService());  // defaults to http://localhost:8545/
  }

  @ResponseBody
  @GetMapping("/client")
  private Object run() throws Exception {
    System.out.println("\u001B[32m" + "test/ run()" + "\u001B[0m");
//    simpleFilterExample();
//    blockInfoExample();
//    countingEtherExample();
    clientVersionExample();

    System.out.println("\u001B[31m" + "test/ run()" + "\u001B[0m");

    if (web3j != null) {
      return web3j.getClass();

    }
    return web3j;

//    System.exit(0);  // we explicitly call the exit to clean up our ScheduledThreadPoolExecutor used by web3j
  }

//  public static void main(String[] args) throws Exception {
//    new TestController().run();
//  }
  void simpleFilterExample() throws Exception {

    Subscription subscription = web3j.blockObservable(false).subscribe(block -> {
      System.out.println("Sweet, block number " + block.getBlock().getNumber()
              + " has just been created");
    }, Throwable::printStackTrace);

    TimeUnit.MINUTES.sleep(2);
    subscription.unsubscribe();
  }

  void blockInfoExample() throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(COUNT);

    System.out.println("Waiting for " + COUNT + " transactions...");
    Subscription subscription = web3j.blockObservable(true)
            .take(COUNT)
            .subscribe(ethBlock -> {
              EthBlock.Block block = ethBlock.getBlock();
              LocalDateTime timestamp = Instant.ofEpochSecond(
                      block.getTimestamp()
                      .longValueExact()).atZone(ZoneId.of("UTC")).toLocalDateTime();
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

    countDownLatch.await(10, TimeUnit.MINUTES);
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

  void clientVersionExample() throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Subscription subscription = web3j.web3ClientVersion().observable().subscribe(x -> {
      System.out.println("Client is running version: {}" + x.getWeb3ClientVersion());
      countDownLatch.countDown();
    });

    countDownLatch.await();
    subscription.unsubscribe();
  }
}
