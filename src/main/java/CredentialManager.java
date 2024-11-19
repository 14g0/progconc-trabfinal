import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CredentialManager {
    private List<String> tokens;
    private AtomicInteger currentIndex = new AtomicInteger(0);

    public CredentialManager(List<String> tokens) {
        this.tokens = tokens;
        System.out.println("[INFO] CredentialManager inicializado com " + tokens.size() + " tokens.");
    }

    public String getNextToken() {
        String threadName = Thread.currentThread().getName();
        int index = currentIndex.getAndUpdate(i -> (i + 1) % tokens.size());
        String token = tokens.get(index);
        System.out.println("[INFO][" + threadName + "] Usando Token: " + index);
        return token;
    }
}
