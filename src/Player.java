import java.util.List;

class Player {
    private String playerId;
    private long balance;
    private List<Action> actions;
    private boolean hasViolatedRules;

    public Player(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean isHasViolatedRules() {
        return hasViolatedRules;
    }

    public void setHasViolatedRules(boolean hasViolatedRules) {
        this.hasViolatedRules = hasViolatedRules;
    }
}