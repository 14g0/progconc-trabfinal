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
        try {
            String token = credentialManager.getNextToken();
            performSearch(dork, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performSearch(String dork, String token) throws IOException, InterruptedException {
        String query = URLEncoder.encode(dork, StandardCharsets.UTF_8);
        String url = "https://api.github.com/search/code?q=" + query;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3.text-match+json")
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            parseAndSaveResults(response.body(), dork);
        } else if (response.statusCode() == 403) {

            System.out.println("Limite de requisições atingido. Aguardando 60 segundos...");
            Thread.sleep(60000);
            performSearch(dork, token);
        } else {
            System.out.println("Erro na requisição: " + response.statusCode());
        }
    }

    private void parseAndSaveResults(String responseBody, String dork) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);
        JsonNode itemsNode = rootNode.path("items");

        File resultadosDir = new File("Resultados");
        if (!resultadosDir.exists()) {
            resultadosDir.mkdir();
        }

        String safeDork = dork.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = "Resultados/resultados_" + safeDork + ".txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            int count = 0;
            for (JsonNode item : itemsNode) {
                if (count >= 10)
                    break;
                String htmlUrl = item.path("html_url").asText();

                String snippet = "";
                JsonNode textMatchesNode = item.path("text_matches");
                if (textMatchesNode.isArray() && textMatchesNode.size() > 0) {
                    snippet = textMatchesNode.get(0).path("fragment").asText();
                }

                writer.write("URL: " + htmlUrl + "\n");
                writer.write("Snippet:\n" + snippet + "\n");
                writer.write("--------------------------------------------------\n");

                count++;
            }
        }
    }
}
