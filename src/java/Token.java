public class Token {
    private String value;
    private int remainingRequests;
    private long resetTime;

    public Token(String value) {
        this.value = value;
        this.remainingRequests = Integer.MAX_VALUE;
        this.resetTime = 0;
    }

    public String getValue() {
        return value;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }

    public boolean isRateLimited() {
        return remainingRequests <= 0 && System.currentTimeMillis() / 1000 < resetTime;
    }
}
