package web3j.ethereum.client.userWeb3jInstance;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.JsonRpc2_0Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import rx.Observable;
import rx.Subscription;

@RestController
@SpringBootApplication
public class UserWeb3jInstanceApplication {

  // OS X/Linux/Unix:
//Web3j web3 = Web3j.build(new UnixIpcService("/path/to/socketfile"));
// Windows
//Web3j web3 = Web3j.build(new WindowsIpcService("/path/to/namedpipefile"));
  @Autowired
  private Web3j web3j;// = Web3j.build(new HttpService());  // defaults to http://localhost:8545/

  private static Credentials ownerCredentials;
  private static Credentials sendToCredentials;

//  public static String walletMnemonic = " surround toilet typical woman guide tree hockey era summer unaware can version";//LOCAL TEST
//  public static String walletPassword = "";
  public static String walletMnemonic = "kangaroo police entry demand cement then alter drum elevator school favorite usage";//istrati.traian@yahoo.com
//  public static String walletMnemonic = "luggage bird predict resemble venue lab fever carpet cushion message dial tribe";//0x55181d8a5d63783f99fc619fb287cee760e3bf2e
  public static String walletPassword = "132eqw!@#";

  public void onTerminateWeb3j() {
//  web3j.shutdown();

    Web3jService web3jService = new WindowsIpcService("geth.ipc");
    Web3j w3Admin = Admin.build(web3jService);
    Web3j w3 = new JsonRpc2_0Admin(web3jService);
//    w3.shutdown();
  }

//  public static String startGethCmdOnPort = "";
  public static void main(String[] args) {
    if (args.length == 1) {
      System.out.println("mnemonic : " + args[0]);

      walletMnemonic = args[0];

      Path lastGethPort = Paths.get("./lastGethPort.txt");

      int nextGethPort = 30303;
      int nextServerPort = 8080;
      try {
        Files.createFile(lastGethPort);
      } catch (IOException e) {
        System.out.println("\u001B[31m" + "ERROR creating file :  " + lastGethPort + " : already exists  ");
      }
      try (BufferedReader br = new BufferedReader(new FileReader(lastGethPort.toFile()))) {
        String line;
        if ((line = br.readLine()) != null) {
          nextGethPort = Integer.parseInt(line);
        }
        if ((line = br.readLine()) != null) {
          nextServerPort = Integer.parseInt(line);
        }
      } catch (IOException e1) {
        System.out.println("ERROR read ");
      }

      try {
        Runtime.getRuntime().exec("geth  --port " + nextGethPort + " --datadir ./ethereum" + nextGethPort);

        Thread.sleep(2000);

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", nextServerPort);

//    SpringApplication.run(UserWeb3jInstanceApplication.class, args);
        new SpringApplicationBuilder()
                .sources(UserWeb3jInstanceApplication.class)
                .properties(props)
                .run(args);

        if (args == null) {
        } else {
        }

        ownerCredentials = WalletUtils.loadBip39Credentials(
                walletPassword, walletMnemonic
        );
        sendToCredentials = WalletUtils.loadBip39Credentials(
                walletPassword, "very typical vacant pull flee scorpion creek giraffe put oak turn praise"
        );
        toAddress = sendToCredentials.getAddress();
        System.out.println("main() ownerAddress = " + ownerCredentials.getAddress());
        System.out.println("main() toAddress = " + toAddress);

      } catch (IOException | InterruptedException e2) {
        System.out.println("ERROR geth.exe ");
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(lastGethPort.toFile()))) {
        bw.write(String.valueOf(++nextGethPort));
        bw.write("\n");
        bw.write(String.valueOf(++nextServerPort));
      } catch (IOException e3) {
        System.out.println("ERROR write ");
      }
    } else {
      System.out.println("bad mnemonics " + Arrays.toString(args));
    }

//    Runtime.getRuntime().exec("geth --verbosity \"0\" --port " + lastPort + " --datadir ~/.ethereum" + lastPort);
  }

  public static final int COUNT = 10;
  private static String toAddress;

  @PostConstruct
  void clientVersionExample() throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Subscription subscription = web3j.web3ClientVersion().observable().subscribe(x -> {
      System.out.println("Client is running version: {}" + x.getWeb3ClientVersion());
      countDownLatch.countDown();
    });

    countDownLatch.await();
    subscription.unsubscribe();

    Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();
    String clientVersion = web3ClientVersion.getWeb3ClientVersion();
    System.out.println("clientVersion = " + clientVersion);
  }

  @GetMapping("/decr")
  public WalletFile decriptWallet() throws ExecutionException, InterruptedException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    //b6fde0d2_email@abc.com"
    String walletJsonStr = "{\"address\":\"867f3cb2fdbbcbe0d0a2395e693741678397cf9a\","
            + "\"id\":\"b6fde0d2-0441-4995-9aab-ae9ee753c9ac\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\","
            + "\"ciphertext\":\"fc56ad869f7511f81acdafa36ea4b163970be20d3b0e47297555c4290b08cb2b\","
            + "\"cipherparams\":{\"iv\":\"056688da2f16be6813256a7e986bf9a8\"},"
            + "\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,"
            + "\"salt\":\"d636ffce8e85eeb73342cc54ed2f8dae50622ade75f3f78c1f200d2574bfe39b\"},"
            + "\"mac\":\"1448a902d76db719be1fe6c5496c286d333f617b835b74b9f4a900315a268bf1\"}}";
    WalletFile walletFile = objectMapper.readValue(walletJsonStr, WalletFile.class);

    try {
      Credentials credentials = Credentials.create(Wallet.decrypt(walletPassword, walletFile));
      //PrK 3246294436838295350555379844499701986563981987091443383075680845384002084435
      System.out.println("clientVersion = " + credentials.getEcKeyPair().getPrivateKey());
      System.out.println("clientVersion = " + credentials.getEcKeyPair().getPublicKey());
    } catch (CipherException ex) {
      System.out.println(" --- ERROR decriptWallet() " + ex);
    }

    return walletFile;
  }

  @GetMapping("/info")
  public EthSyncing info() throws ExecutionException, InterruptedException {
    return web3j.ethSyncing().sendAsync().get();
  }

  @GetMapping("/accounts")
  public EthAccounts exe() throws ExecutionException, InterruptedException {

    return web3j.ethAccounts().sendAsync().get();
  }

  @GetMapping("/send")
  public TransactionReceipt sendFunds() {
    try {
      TransactionReceipt transactionReceipt = sendEther(0.0001, toAddress);
      return transactionReceipt;
    } catch (Exception e) {
      System.out.println(" --- ERROR sendFunds() () " + e);

    }
    return null;
  }

  public TransactionReceipt sendEther(double val, String toAddress) throws Exception {
    TransactionReceipt transactionReceipt = Transfer.sendFunds(
            web3j, ownerCredentials, toAddress,
            BigDecimal.valueOf(val), Convert.Unit.ETHER)
            .sendAsync().get();
    return transactionReceipt;
  }

  @GetMapping("/balance")
  public EthGetBalance getBalance() {
    try {
      return getBalanceTransaction(ownerCredentials.getAddress());
    } catch (Exception e) {
      System.out.println(" --- ERROR getBallance() " + e);
    }
    return null;
  }

  public EthGetBalance getBalanceTransaction(String address) throws Exception {

    return web3j.ethGetBalance(
            address, DefaultBlockParameterName.LATEST)
            .sendAsync().get();
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
