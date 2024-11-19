import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CredentialManager {
    private List<Token> tokens;
    private AtomicInteger currentIndex = new AtomicInteger(0);

    public CredentialManager(List<String> tokenValues) {
        this.tokens = tokenValues.stream().map(Token::new).collect(Collectors.toList());
    }

    public synchronized Token getNextAvailableToken() throws InterruptedException {

        String threadName = Thread.currentThread().getName();
        int startIndex = currentIndex.get();
        int index = startIndex;

        do {
            Token token = tokens.get(index);
            System.out.println("[INFO][" + threadName + "] Usando Token: " + index);

            if (!token.isRateLimited()) {
                currentIndex.set((index + 1) % tokens.size());
                return token;
            }
            index = (index + 1) % tokens.size();
            if (index == startIndex) {
                long waitTime = tokens.stream()
                        .mapToLong(t -> t.getResetTime() - (System.currentTimeMillis() / 1000))
                        .filter(t -> t > 0)
                        .min().orElse(60);
                System.out.println("[WARNING][" + threadName + "] Todos os tokens estão rate-limited. Esperando "
                        + waitTime + " segundos...");
                Thread.sleep(waitTime * 1000);
            }
        } while (true);
    }
}
