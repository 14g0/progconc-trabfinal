import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitHubSearchTask implements Runnable {
    private String dork;
    private CredentialManager credentialManager;

    public GitHubSearchTask(String dork, CredentialManager credentialManager) {
        this.dork = dork;
        this.credentialManager = credentialManager;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[INFO][" + threadName + "] Iniciando task para a dork: " + dork);
        try {
            String token = credentialManager.getNextToken();
            performSearch(dork, token);
        } catch (Exception e) {
            System.err.println("[ERROR][" + threadName + "] Erro na dork: " + dork);
            e.printStackTrace();
        }
    }

    private void performSearch(String dork, String token) throws IOException, InterruptedException {
        String threadName = Thread.currentThread().getName();
        String query = URLEncoder.encode(dork, StandardCharsets.UTF_8);
        String url = "https://api.github.com/search/code?q=" + query;

        System.out.println("[INFO][" + threadName + "] Requisição para: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3.text-match+json")
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[INFO][" + threadName + "] Código de resposta: " + response.statusCode());

        if (response.statusCode() == 200) {
            System.out.println("[INFO][" + threadName + "] Resposta 200 sucesso");
            parseAndSaveResults(response.body(), dork);
        } else if (response.statusCode() == 403) {
            System.out.println(
                    "[WARNING][" + threadName + "] Limite de requisições. Esperando 60s...");
            Thread.sleep(60000);
            System.out.println("[INFO][" + threadName + "] Tentando novamente para a dork: " + dork);
            performSearch(dork, token);
        } else {
            System.err.println("[ERROR][" + threadName + "] Falha na request: " + dork
                    + ". Código: " + response.statusCode());
        }
    }

    private void parseAndSaveResults(String responseBody, String dork) throws IOException {
        String threadName = Thread.currentThread().getName();
        System.out.println("[INFO][" + threadName + "] Processando resultados da dork: " + dork);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);
        JsonNode itemsNode = rootNode.path("items");

        File resultadosDir = new File("Resultados");
        if (!resultadosDir.exists()) {
            System.out.println("[INFO][" + threadName + "] Diretório não existe, criando diretório para resultados...");
            resultadosDir.mkdir();
        }

        String safeDork = dork.replaceAll("[^a-zA-Z0-9]", "_"); // Replace nos caracteres que não são letra nem números
        String fileName = "Resultados/resultados_" + safeDork + ".txt";

        System.out.println("[INFO][" + threadName + "] Salvando resultados no arquivo: " + fileName);

        try (FileWriter writer = new FileWriter(fileName)) {
            int count = 0;
            for (JsonNode item : itemsNode) {
                if (count >= 10)
                    break;
                String htmlUrl = item.path("html_url").asText();

                String trecho = "";
                JsonNode textMatchesNode = item.path("text_matches");
                if (textMatchesNode.isArray() && textMatchesNode.size() > 0) {
                    trecho = textMatchesNode.get(0).path("fragment").asText();
                }

                writer.write("URL: " + htmlUrl + "\n");
                writer.write("Trecho:\n" + trecho + "\n");
                writer.write("--------------------------------------------------\n");

                count++;
            }

            System.out.println(
                    "[INFO][" + threadName + "] Resultados para a dork '" + dork + "' salvos com sucesso!");
        } catch (IOException e) {
            System.err.println("[ERROR][" + threadName + "] Erro ao salvar resultados para a dork: " + dork);
            throw e;
        }
    }
}
