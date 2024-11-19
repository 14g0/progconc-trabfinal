import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CredentialManager {
    private List<String> tokens;
    private AtomicInteger currentIndex = new AtomicInteger(0);

    public CredentialManager(List<String> tokens) {
        this.tokens = tokens;
    }

    public String getNextToken() {
        int index = currentIndex.getAndUpdate(i -> (i + 1) % tokens.size());
        return tokens.get(index);
    }
}