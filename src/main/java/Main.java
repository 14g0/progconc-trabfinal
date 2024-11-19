import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("[INFO][Main] Iniciando o programa...");

        try {
            System.out.println("[INFO][Main] Lendo arquivo dorks.txt...");
            List<String> dorkList = Files.readAllLines(Paths.get("dorks.txt"));
            System.out.println("[INFO][Main] Dorks carregados: " + dorkList.size());

            System.out.println("[INFO][Main] Lendo arquivo tokens.txt...");
            List<String> tokens = Files.readAllLines(Paths.get("tokens.txt"));
            System.out.println("[INFO][Main] Tokens carregados: " + tokens.size());

            CredentialManager credentialManager = new CredentialManager(tokens);

            int numberOfThreads = 20;
            System.out.println("[INFO][Main] Criando ExecutorService com " + numberOfThreads + " threads...");
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

            long startTime = System.currentTimeMillis();

            System.out.println("[INFO][Main] Submetendo tarefas");
            for (String dork : dorkList) {
                executorService.submit(new GitHubSearchTask(dork, credentialManager));
            }

            System.out.println("[INFO][Main] Aguardando conclusão das tarefas...");
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            long endTime = System.currentTimeMillis();
            System.out.println("[INFO][Main] Tarefas concluídas.");
            System.out.println("[INFO][Main] Tempo de execução: " + (endTime - startTime) + "ms");

        } catch (IOException e) {
            System.err.println("[ERROR][Main] Erro ao ler arquivos: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[ERROR][Main] Erro durante a espera pela conclusão das tarefas: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[INFO][Main] FIM.");
    }
}
