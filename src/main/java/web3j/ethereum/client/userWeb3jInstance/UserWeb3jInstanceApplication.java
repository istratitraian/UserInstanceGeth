package web3j.ethereum.client.userWeb3jInstance;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

@RestController
@SpringBootApplication
public class UserWeb3jInstanceApplication {

  @Autowired
  private Web3j web3j;

  private static Credentials ownerCredentials;
  private static Credentials sendToCredentials;

  public static String walletMnemonic = "kangaroo police entry demand cement then alter drum elevator school favorite usage";//istrati.traian@yahoo.com
  public static String walletPassword = "132eqw!@#";

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length == 2) {
      System.out.println("mnemonic : " + args[0]);

      walletMnemonic = args[1];
      
      String userId = args[0];

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

      try {
        Process exec = Runtime.getRuntime().exec("geth  --verbosity 0 --cache=2048 --nousb");
//          Process exec = Runtime.getRuntime().exec("geth  --verbosity 0 --cache=512 --nousb --networkid 1");
        System.out.println("\u001B[35m" + "geth start : " + exec.isAlive() + "\u001B[0m");
      } catch (IOException ex) {
      }

      Map<String, Object> props = new HashMap<>();

      String ipc = "\\\\.\\pipe\\geth.ipc";

      if (System.getProperty("os.name").toLowerCase().startsWith("lin")) {

        String linuxHome = System.getProperty("user.home");
        System.out.println("\u001B[35m" + "linuxHome " + linuxHome + "\u001B[0m");

        ipc = linuxHome + "/.ethereum/geth.ipc";

//        web3j = Web3j.build(new UnixIpcService("/home/it/.ethereum/geth.ipc"));
      } else {
//        web3j = Web3j.build(new WindowsIpcService("\\\\.\\pipe\\geth.ipc"));
      }

      File ipcFile = Paths.get(ipc).toFile();

      int count = 1;
      while (!ipcFile.exists()) {
        try {
          System.out.print("\rwaith for IPC " + count++);
          Thread.sleep(1000);
        } catch (InterruptedException ex1) {
        }
      }
      System.out.println("");

      props.put("web3j.client-address", ipc);
//
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
  String web3jClientAddress;

  @PostConstruct
  void clientVersionExample() throws Exception {

    System.out.println("\u001B[33m" + web3j.getClass());
//    System.out.println("admin.personalListAccounts() "+web3j.personalListAccounts().sendAsync().get().getResult());
    System.out.println("web3jClientAddress = " + web3jClientAddress);
    Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();
    String clientVersion = web3ClientVersion.getWeb3ClientVersion();
    System.out.println("clientVersion = " + clientVersion + "\u001B[0m");
  }

  @GetMapping("/decript")
  public WalletFile decriptWallet() throws ExecutionException, InterruptedException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    String walletJsonStr = "{\"address\":\"30815cfa8d078d4a54a1b5315e31720a812b46df\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"9618687fe69fa0f914435e544530727edee9c62c17189ea4c1928aa6b6096c05\",\"cipherparams\":{\"iv\":\"46dd49098fd66ea30952a6f351d1f317\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"7cd319e5d6e249f939763f425aee9d7705e514f370a1c49987bbd9763af1cec1\"},\"mac\":\"405c1d4e9f3394c43d4c8c88568a1f852233d00f5ff6daf3a95f8ce3d82f3f37\"},\"id\":\"a976e491-17ba-420f-b439-429f7c7f1810\",\"version\":3}";
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

  @GetMapping("/decript/{fileContent}")
  public Object decriptWallet(@PathVariable String fileContent) throws ExecutionException, InterruptedException, IOException, CipherException {
    ObjectMapper objectMapper = new ObjectMapper();
    WalletFile walletFile = objectMapper.readValue(fileContent, WalletFile.class);
    Credentials credentials = Credentials.create(Wallet.decrypt(walletPassword, walletFile));
    System.out.println("clientVersion = " + credentials.getEcKeyPair().getPrivateKey());
    System.out.println("clientVersion = " + credentials.getEcKeyPair().getPublicKey());

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
  public Object sendFunds() {
    try {
      TransactionReceipt transactionReceipt = sendEther(0.0001, toAddress);
      return transactionReceipt;
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

  @GetMapping("/send/{toAddress}")
  public Object sendFunds(@PathVariable String toAddress) {
    try {
      TransactionReceipt transactionReceipt = sendEther(0.0001, toAddress);
      return transactionReceipt;
    } catch (Exception ex) {
      return ex.getMessage();
    }
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
