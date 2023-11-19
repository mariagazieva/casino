import java.io.*;
import java.util.*;

public class CasinoBrain {

    private static final String PLAYER_DATA_FILE = "player_data.txt";
    private static final String MATCH_DATA_FILE = "match_data.txt";
    private static final String OUTPUT_FILE = "results.txt";

    private static final List<Player> players = new ArrayList<>();
    private static final List<Match> matches = new ArrayList<>();

    public static void main(String[] args) {
        readPlayersFromFile();
        readMatchesFromFile();
        processPlayerActions();
        writeResultsToFile();
    }

    private static void readPlayersFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(PLAYER_DATA_FILE))) {
            String line;
            Map<String, List<Action>> playerActionsMap = new LinkedHashMap<>();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String playerId = data[0];
                Action action = createActionFromData(data);
                playerActionsMap.computeIfAbsent(playerId, k -> new ArrayList<>()).add(action);
            }
            playerActionsMap.forEach((playerId, actions) -> {
                Player player = new Player(playerId);
                player.setActions(actions);
                players.add(player);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Action createActionFromData(String[] data) {
        ActionType type = ActionType.valueOf(data[1]);
        String matchId = data[2];
        int amount = data[3].isEmpty() ? 0 : Integer.parseInt(data[3]);
        String side = data.length > 4 ? data[4] : null;
        return new Action(type, matchId, amount, side);
    }

    private static void readMatchesFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(MATCH_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String matchId = data[0];
                double rateSideA = Double.parseDouble(data[1]);
                double rateSideB = Double.parseDouble(data[2]);
                char result = data[3].charAt(0);
                matches.add(new Match(matchId, rateSideA, rateSideB, result));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processPlayerActions() {
        for (Player player : players) {
            long balance = 0;
            boolean hasViolatedRules = false;

            for (Action action : player.getActions()) {
                ActionType type = action.getType();
                if (type == ActionType.DEPOSIT) {
                    balance += action.getAmount();
                } else if (type == ActionType.BET || type == ActionType.WITHDRAW) {
                    if (!isActionLegal(action, balance)) {
                        hasViolatedRules = true;
                        break;
                    }
                    if (type == ActionType.BET) {
                        processBetAction(player, action);
                    } else {
                        balance -= action.getAmount();
                    }
                }
            }
            player.setBalance(balance);
            player.setHasViolatedRules(hasViolatedRules);
        }
    }

    private static boolean isActionLegal(Action action, long balance) {
        if (action.getType() == ActionType.BET || action.getType() == ActionType.WITHDRAW) {
            return action.getAmount() <= balance && action.getAmount() >= 0;
        }
        return true;
    }

    private static void processBetAction(Player player, Action action) {
        Match match = findMatchById(action.getMatchId());
        if (match == null) {
            return;
        }
        if (isBetWinning(action.getSide(), match)) {
            player.setBalance(player.getBalance() + calculateWinAmount(action.getAmount(), action.getSide(), match));
        } else if (!isMatchDraw(match)) {
            player.setBalance(player.getBalance() - action.getAmount());
        }
    }

    private static boolean isBetWinning(String side, Match match) {
        char result = match.getResult();
        return (result == 'A' && side.equals("A")) || (result == 'B' && side.equals("B"));
    }

    private static boolean isMatchDraw(Match match) {
        return match.getResult() == 'D';
    }

    private static int calculateWinAmount(int betAmount, String side, Match match) {
        return side.equals("A") ? (int) (betAmount * match.getRateSideA()) : (int) (betAmount * match.getRateSideB());
    }

    private static Match findMatchById(String matchId) {
        return matches.stream().filter(match -> match.getMatchId().equals(matchId)).findFirst().orElse(null);
    }

    private static void writeResultsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            writeLegitimatePlayers(writer);
            writer.newLine();
            writeIllegitimatePlayers(writer);
            writer.newLine();
            writeCasinoBalanceChange(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


private static void writeLegitimatePlayers(BufferedWriter writer) throws IOException {
    try {
        boolean hasLegitimatePlayers = false;
        for (Player player : players) {
            if (!player.isHasViolatedRules()) {
                writer.write(player.getPlayerId() + " " + player.getBalance() + " " + calculateWinRate(player.getActions()));
                writer.newLine();
                hasLegitimatePlayers = true;
            }
        }
        if (!hasLegitimatePlayers) {
            throw new IOException("No legitimate players found");
        }
    } catch (IOException e) {
        writer.newLine();
    }
}

    private static void writeIllegitimatePlayers(BufferedWriter writer) throws IOException {
        try {
            boolean hasIllegitimatePlayers = false;
            for (Player player : players) {
                if (player.isHasViolatedRules()) {
                    writer.write(player.getPlayerId() + " " + getIllegalOperation(player.getActions()));
                    writer.newLine();
                    hasIllegitimatePlayers = true;
                }
            }
            if (!hasIllegitimatePlayers) {
                throw new IOException("No illegitimate players found");
            }
        } catch (IOException e) {
            writer.newLine();
        }
    }

    private static String getIllegalOperation(List<Action> actions) {
        for (Action action : actions) {
            if (action.getType() == ActionType.BET || action.getType() == ActionType.WITHDRAW) {
                return action.getType() + " " + action.getMatchId() + " " +
                        (action.getAmount() != 0 ? action.getAmount() : "null") + " " +
                        (action.getSide() != null ? action.getSide() : "null");
            }
        }
        return "";
    }

    private static double calculateWinRate(List<Action> actions) {
        long totalBets = actions.stream().filter(action -> action.getType() == ActionType.BET).count();
        long wonBets = actions.stream().filter(action -> action.getType() == ActionType.BET &&
                isBetWinning(action.getSide(), findMatchById(action.getMatchId()))).count();
        if (totalBets > 0) return Math.round(((double) wonBets / totalBets) * 100.0) / 100.0;
        return 0.0;

    }

    private static void writeCasinoBalanceChange(BufferedWriter writer) throws IOException {
        long casinoBalanceChange = 0;
        for (Player player : players) {
            for (Action action : player.getActions()) {
                if (action.getType() == ActionType.BET) {
                    if (isBetWinning(action.getSide(), findMatchById(action.getMatchId()))) {
                        casinoBalanceChange -= calculateWinAmount(action.getAmount(), action.getSide(),
                                findMatchById(action.getMatchId()));
                    } else {
                        casinoBalanceChange += action.getAmount();
                    }
                }
            }
        }
        writer.write(String.valueOf(casinoBalanceChange));
    }
}

