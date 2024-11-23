import java.io.BufferedWriter;
import java.io.FileWriter;
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

        String dorksFilePath = "Dorks/dorks_20.txt";
        String tokensFilePath = "Tokens/tokens_1.txt";
        int numberOfThreads = 10;

        try {

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-d":
                        if (i + 1 < args.length) {
                            dorksFilePath = args[++i];
                        } else {
                            System.out.println("[ERROR] Caminho para o arquivo de dorks não especificado.");
                            return;
                        }
                        break;
                    case "-t":
                        if (i + 1 < args.length) {
                            tokensFilePath = args[++i];
                        } else {
                            System.out.println("[ERROR] Caminho para o arquivo de tokens não especificado.");
                            return;
                        }
                        break;
                    case "-n":
                        if (i + 1 < args.length) {
                            try {
                                numberOfThreads = Integer.parseInt(args[++i]);
                            } catch (NumberFormatException e) {
                                System.out.println("[ERROR] Número de threads inválido.");
                                return;
                            }
                        } else {
                            System.out.println("[ERROR] Número de threads não especificado.");
                            return;
                        }
                        break;
                    default:
                        System.out.println("[ERROR] Parâmetro desconhecido: " + args[i]);
                        return;
                }
            }

            System.out.println("[INFO][Main] Lendo arquivo dorks.txt...");
            List<String> dorkList = Files.readAllLines(Paths.get(dorksFilePath));
            System.out.println("[INFO][Main] Dorks carregados: " + dorkList.size());

            System.out.println("[INFO][Main] Lendo arquivo tokens.txt...");
            List<String> tokens = Files.readAllLines(Paths.get(tokensFilePath));
            System.out.println("[INFO][Main] Tokens carregados: " + tokens.size());

            CredentialManager credentialManager = new CredentialManager(tokens);

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
            System.out.println("----------------------------------------");
            System.out.println("Número de threads: " + numberOfThreads);
            System.out.println("Número de dorks: " + dorkList.size());
            System.out.println("Número de tokens: " + tokens.size());
            System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");

            String csvLine = numberOfThreads + "," + dorkList.size() + "," + tokens.size() + "," + (endTime - startTime);

            // Apende a linha CSV no arquivo de saída
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("testResults.csv", true))) {
                writer.write(csvLine);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("[ERROR][Main] Erro ao ler arquivos: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[ERROR][Main] Erro durante a espera pela conclusão das tarefas: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
