package flashcards;

public class Card {

    private String term;
    private String def;
    private int mistakesCount;

    public Card(String term, String def, int mistakesCount) {
        this.term = term;
        this.def = def;
        this.mistakesCount = mistakesCount;
    }

    public String getDef() {
        return def;
    }

    public String getTerm() {
        return term;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public int getMistakesCount() {
        return mistakesCount;
    }

    public void addMistake() {
        this.mistakesCount++;
    }

    public void resetMistakes() {
        mistakesCount = 0;
    }

    public void setMistakesCount(int mistakesCount) {
        this.mistakesCount = mistakesCount;
    }
}
