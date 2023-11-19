class Action {
    private ActionType type;
    private String matchId;
    private int amount;
    private String side;

    public Action(ActionType type, String matchId, int amount, String side) {
        this.type = type;
        this.matchId = matchId;
        this.amount = amount;
        this.side = side;
    }

    public ActionType getType() {
        return type;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getAmount() {
        return amount;
    }

    public String getSide() {
        return side;
    }
}

enum ActionType {
    DEPOSIT, BET, WITHDRAW
}
