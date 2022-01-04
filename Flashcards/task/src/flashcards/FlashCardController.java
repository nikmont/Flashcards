package flashcards;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FlashCardController {
    static List<Card> cards = new ArrayList<>();
    static List<String> logger = new ArrayList<>();
    private final Map<String, String> args;

    public FlashCardController(Map<String, String> args) {
        this.args = args;
    }

    public void start() {

        if (args.containsKey("-import")) {
            importCards(args.get("-import"));
        }

        boolean isExit = false;

        while (!isExit) {
            outputMsg("");
            outputMsg("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String input = getUserInput();

            switch (input) {
                case "add":
                    add();
                    break;
                case "remove":
                    outputMsg("Which card?");
                    String cardToRemove = getUserInput();

                    if (remove(cardToRemove)) {
                        outputMsg("The card has been removed.");
                    } else {
                        outputMsg("Can't remove \""+ cardToRemove + "\": there is no such card.");
                    }
                    break;
                case "import":
                    outputMsg("File name:");
                    importCards(getUserInput());
                    break;
                case "export":
                    outputMsg("File name:");
                    exportCards(getUserInput());
                    break;
                case "ask":
                    outputMsg("How many times to ask?");
                    ask(Integer.parseInt(getUserInput()));
                    break;
                case "log" :
                    outputMsg("File name:");
                    logStats(getUserInput());
                    break;
                case "hardest card" :
                    List<Card> hardest = getHardestCards();

                    if (hardest.isEmpty() || hardest.get(0).getMistakesCount() == 0) {
                        outputMsg("There are no cards with errors.");
                    } else if (hardest.size() > 1) {
                        String joinedCards = hardest.stream()
                                .map(Card::getTerm)
                                .reduce((s1, s2) -> "\"" + s1 + "\", \"" + s2 + "\"").get();

                        outputMsg("The hardest cards are " + joinedCards + ". You have " + hardest.get(0).getMistakesCount() + " errors answering them.");
                    } else {
                        outputMsg("The hardest card is \"" + hardest.get(0).getTerm() + "\". You have " + hardest.get(0).getMistakesCount() + " errors answering it.");
                    }
                    break;
                case "reset stats" :
                    resetStats();
                    break;
                case "exit":
                    outputMsg("Bye bye!");

                    if (args.containsKey("-export")) {
                        exportCards(args.get("-export"));
                    }
                    isExit = true;
                    break;
                default:
                    outputMsg("Invalid action");
            }
        }
    }

    private static void logStats(String filename) {

        try (PrintWriter printWriter = new PrintWriter(new FileWriter(filename))) {

            logger.forEach(printWriter::println);

        } catch (IOException e) {
            e.printStackTrace();
        }

        outputMsg("The log has been saved.");
    }

    private static void resetStats() {
        cards.forEach(Card::resetMistakes);
        outputMsg("Card statistics have been reset.");
    }

    private static List<Card> getHardestCards() {
        Function<Card, Integer> cardFunc = Card::getMistakesCount;

        if (cards.size() == 0) return Collections.emptyList();

        return new ArrayList<>(cards.stream()
                .collect(Collectors.groupingBy(cardFunc, TreeMap::new, toList()))
                .lastEntry()
                .getValue());
    }

    private static void add() {
        outputMsg("The card:");
        String name = getUserInput();
        if (checkTermExists(name)) {
            outputMsg("The card \"" + name + "\" already exists.");
            return;
        }

        outputMsg("The definition of the card:");
        String def = getUserInput();
        if (checkAnswerExists(def)) {
            outputMsg("The definition \"" + def + "\" already exists.");
            return;
        }

        Card card = new Card(name, def, 0);
        cards.add(card);
        outputMsg("The pair (\"" + name + "\":\"" + def + "\") has been added.");
    }

    private static boolean checkTermExists(String term) {
        return cards.stream()
                .map(Card::getTerm)
                .anyMatch(term::equals);
    }

    private static boolean checkAnswerExists(String answer) {
        return cards.stream()
                .map(Card::getDef)
                .anyMatch(answer::equals);
    }

    private static void importCards(String filename) {
        long loadCardsCount = 0;

        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            List<String> input = stream.collect(toList());
            loadCardsCount = input.size();

            List<Card> importedCards = new ArrayList<>();
            for (String s : input) {
                String[] card = s.split(":");
                importedCards.add(new Card(card[0], card[1], Integer.parseInt(card[2])));
            }

            addToCurrentCards(importedCards);

        } catch (NoSuchFileException ex) {
            outputMsg("File not found.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        outputMsg(loadCardsCount + " cards have been loaded.");
    }

    private static void addToCurrentCards(List<Card> load) {

        for (Card card : load) {

            if (contains(card)) {
                for (Card card1 : cards) {
                    if (card1.getTerm().equals(card.getTerm())) {
                        card1.setDef(card.getDef());
                        card1.setMistakesCount(card.getMistakesCount());
                    }
                }
            } else {
                cards.add(card);
            }
        }
    }

    private static boolean contains(Card card) {
        return cards.stream()
                .anyMatch(c -> c.getTerm().equals(card.getTerm()));
    }

    private static void exportCards(String filename) {

        try (PrintWriter printWriter = new PrintWriter(new FileWriter(filename))) {
            cards.stream()
                    .map(c -> c.getTerm().concat(":").concat(c.getDef()).concat(":").concat(c.getMistakesCount()+""))
                    .forEach(printWriter::println);

        } catch (IOException e) {
            e.printStackTrace();
        }

        outputMsg(cards.size() + " cards have been saved.");
    }

    private static void ask(int times) {

        for (int i = 0, cardCounter = 0; i < times && cardCounter != times; i++) {

            if (cards.size() == i) i = 0;

            outputMsg("Print the definition of \"" + cards.get(i).getTerm() + "\":");
            String answer = getUserInput();

            String wrongAnswer = String.format("Wrong. The right answer is \"%s\"", cards.get(i).getDef());
            String anotherCard =
                    cards.stream().anyMatch(d -> d.getDef().equals(answer)) ? String.format(", but your definition is correct for \"%s\".", findAnotherTermForDefinition(answer)) : ".";

            if (cards.get(i).getDef().equals(answer)) {
                outputMsg("Correct!");
            } else {
                outputMsg(wrongAnswer + anotherCard);
                cards.get(i).addMistake();
            }

            cardCounter++;
        }
    }

    private static String findAnotherTermForDefinition(String def) {

        return cards.stream()
                .filter(d -> def.equals(d.getDef()))
                .map(Card::getTerm)
                .findFirst().orElse("");
    }

    private static boolean remove(String name) {
        return cards.removeIf(c -> name.equals(c.getTerm()));
    }

    private static String getUserInput() {
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        logger.add(input);

        return input;
    }

    private static void outputMsg(String str) {
        logger.add(str);

        System.out.println(str);
    }
}
