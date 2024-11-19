import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        try {
            List<String> dorkList = Files.readAllLines(Paths.get("dorks.txt"));

            List<String> tokens = Files.readAllLines(Paths.get("tokens.txt"));

            CredentialManager credentialManager = new CredentialManager(tokens);

            int numberOfThreads = 3;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

            long startTime = System.currentTimeMillis();

            for (String dork : dorkList) {
                executorService.submit(new GitHubSearchTask(dork, credentialManager));
            }

            executorService.shutdown();

            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            long endTime = System.currentTimeMillis();
            System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
