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
            Token token = credentialManager.getNextAvailableToken();
            performSearch(dork, token);
        } catch (Exception e) {
            System.err.println("[ERROR][" + threadName + "] Erro na dork: " + dork);
            e.printStackTrace();
        }
    }

    private void performSearch(String dork, Token token) throws IOException, InterruptedException {
        String threadName = Thread.currentThread().getName();
        String query = URLEncoder.encode(dork, StandardCharsets.UTF_8);
        String url = "https://api.github.com/search/code?q=" + query;

        System.out.println("[INFO][" + threadName + "] Requisição para: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token.getValue())
                .header("Accept", "application/vnd.github.v3.text-match+json")
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[INFO][" + threadName + "] Código de resposta: " + response.statusCode());
        updateTokenRateLimit(token, response.headers());

        if (response.statusCode() == 200) {
            System.out.println("[INFO][" + threadName + "] Resposta 200 sucesso");
            parseAndSaveResults(response.body(), dork);
        } else if (response.statusCode() == 403) {
            if (isRateLimitExceeded(response)) {
                System.out.println(
                        "[WARNING][" + threadName + "] Limite de requisições. Trocando Token...");

                token.setRemainingRequests(0);
                token.setResetTime(getRateLimitResetTime(response.headers()));
                Token newToken = credentialManager.getNextAvailableToken();
                performSearch(dork, newToken);
            } else {
                System.out.println("[ERROR][" + threadName + "] Acesso negado ou erro na requisição. Código HTTP:"
                        + response.statusCode());
            }
        } else {
            System.out.println("Erro na requisição: " + response.statusCode());
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

    private void updateTokenRateLimit(Token token, HttpHeaders headers) {
        int remaining = headers.firstValue("X-RateLimit-Remaining").map(Integer::parseInt).orElse(0);
        long reset = headers.firstValue("X-RateLimit-Reset").map(Long::parseLong).orElse(0L);

        token.setRemainingRequests(remaining);
        token.setResetTime(reset);
    }

    private boolean isRateLimitExceeded(HttpResponse<?> response) {
        return response.headers().firstValue("X-RateLimit-Remaining").orElse("0").equals("0");
    }

    private long getRateLimitResetTime(HttpHeaders headers) {
        return headers.firstValue("X-RateLimit-Reset").map(Long::parseLong).orElse(0L);
    }

}
