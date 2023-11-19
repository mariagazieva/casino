class Match {
    private String matchId;
    private double rateSideA;
    private double rateSideB;
    private char result;

    public Match(String matchId, double rateSideA, double rateSideB, char result) {
        this.matchId = matchId;
        this.rateSideA = rateSideA;
        this.rateSideB = rateSideB;
        this.result = result;
    }

    public String getMatchId() {
        return matchId;
    }

    public double getRateSideA() {
        return rateSideA;
    }

    public double getRateSideB() {
        return rateSideB;
    }

    public char getResult() {
        return result;
    }
}
