package flashcards;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        FlashCardController controller = new FlashCardController(parseArgs(args));
        controller.start();
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> CLARGS = new HashMap<>();

        for (int i = 0; i < args.length-1; i+=2) {
            CLARGS.put(args[i], args[i+1]);
        }
        return CLARGS;
    }
}
