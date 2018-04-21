package web3j.ethereum.client.userWeb3jInstance;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
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
//    Web3j w3 = new JsonRpc2_0Admin(web3jService);
//    w3.shutdown();
  }

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length == 1) {
      System.out.println("mnemonic : " + args[0]);

      walletMnemonic = args[0];

      Path lastGethPort = Paths.get("./lastGethPort.txt");

      int nextGethPort = 30303;
      int nextServerPort = 8080;
      try {
        Files.createFile(lastGethPort);
      } catch (IOException e) {
        System.out.println("\u001B[31m" + "ERROR creating file :  " + lastGethPort + " : already exists  " + "\u001B[0m");
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
        System.out.println("\u001B[31m" + "ERROR read " + "\u001B[0m");
      }

      try {
        Runtime.getRuntime().exec("geth  --port " + nextGethPort + " --datadir ./ethereum" + nextGethPort);

        Thread.sleep(2000);

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", nextServerPort);

        new SpringApplicationBuilder()
                .sources(UserWeb3jInstanceApplication.class)
                .properties(props)
                .run(args);

      } catch (IOException | InterruptedException e2) {
        System.out.println("\u001B[31m" + "ERROR geth.exe " + "\u001B[0m");
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(lastGethPort.toFile()))) {
        bw.write(String.valueOf(++nextGethPort));
        bw.write("\n");
        bw.write(String.valueOf(++nextServerPort));
      } catch (IOException e3) {
        System.out.println("\u001B[31m" + "ERROR write " + "\u001B[0m");
      }

    } else {

      Map<String, Object> props = new HashMap<>();

      try {
        Process exec = Runtime.getRuntime().exec("geth");
        System.out.println("\u001B[35m" + "geth start : " + exec.isAlive() + "\u001B[0m");
      } catch (IOException ex) {
      }

      String ipc = "\\\\.\\pipe\\geth.ipc";

      if (System.getProperty("os.name").toLowerCase().startsWith("lin")) {
        ipc = "~/.ethereum/get.ipc";
      }

      try {
        System.out.println("waith for IPC " + ipc);
        Thread.sleep(10000);
      } catch (InterruptedException ex1) {
      }

//      Web3jAutoConfiguration df;
//
//org.web3j.protocol.core.JsonRpc2_0Web3j jd;
      props.put("web3j.client-address", ipc);

      new SpringApplicationBuilder()
              .sources(UserWeb3jInstanceApplication.class)
              .properties(props)
              .run(args);
//      SpringApplication.run(UserWeb3jInstanceApplication.class, args);
      System.out.println("\u001B[34m" + "SpringApplication launched : " + "\u001B[0m");
    }

    ownerCredentials = WalletUtils.loadBip39Credentials(
            walletPassword, walletMnemonic
    );
    sendToCredentials = WalletUtils.loadBip39Credentials(
            walletPassword, "very typical vacant pull flee scorpion creek giraffe put oak turn praise"
    );
    toAddress = sendToCredentials.getAddress();
    System.out.println("\u001B[32m" + "main() ownerAddress = " + ownerCredentials.getAddress());
    System.out.println("main() toAddress = " + toAddress + "\u001B[0m");

//    Runtime.getRuntime().exec("geth --verbosity \"0\" --port " + lastPort + " --datadir ~/.ethereum" + lastPort);
  }

  public static final int COUNT = 10;
  private static String toAddress;

//  @Value("${web3j.network-id}")
  @Value("${web3j.client-address}")
  String web3jNetworkId;

  @PostConstruct
  void clientVersionExample() throws Exception {

    System.out.println("\u001B[31m" + web3j.getClass());
    System.out.println("web3jNetworkId = " + web3jNetworkId);
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

    String walletJsonStr = "\"{\"address\":\"30815cfa8d078d4a54a1b5315e31720a812b46df\","
            + "\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":"
            + "\"9618687fe69fa0f914435e544530727edee9c62c17189ea4c1928aa6b6096c05\","
            + "\"cipherparams\":{\"iv\":\"46dd49098fd66ea30952a6f351d1f317\"},"
            + "\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,"
            + "\"salt\":\"7cd319e5d6e249f939763f425aee9d7705e514f370a1c49987bbd9763af1cec1\"},"
            + "\"mac\":\"405c1d4e9f3394c43d4c8c88568a1f852233d00f5ff6daf3a95f8ce3d82f3f37\"},"
            + "\"id\":\"a976e491-17ba-420f-b439-429f7c7f1810\",\"version\":3}\"";
    WalletFile walletFile = objectMapper.readValue(walletJsonStr, WalletFile.class);

    try {
      Credentials credentials = Credentials.create(Wallet.decrypt(walletPassword, walletFile));
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

}
