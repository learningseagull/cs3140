import java.util.*;


class Dictionary {
    private Set<String> words;
    
    public Dictionary() {
        words = new HashSet<>();
    }
    
    public boolean contains(String word) {
        return words.contains(word.toLowerCase());
    }
    
    public void addWord(String word) {
        words.add(word.toLowerCase());
    }
    
    public int size() {
        return words.size();
    }
}

class WordValidator {
    public static boolean isValidWord(String word) {
        return word.matches("[a-zA-Z]{5}");
    }
}


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DictionaryTest {
    @Test
    void testAddAndContains() {
        Dictionary dict = new Dictionary();
        dict.addWord("apple");
        assertTrue(dict.contains("apple"));
        assertFalse(dict.contains("banana"));
    }
    
    @Test
    void testSize() {
        Dictionary dict = new Dictionary();
        dict.addWord("apple");
        dict.addWord("banana");
        assertEquals(2, dict.size());
    }
}


enum LetterResult { GRAY, GREEN, YELLOW }

class GuessResult {
    private String guess;
    private String answer;
    
    public GuessResult(String guess, String answer) {
        this.guess = guess.toLowerCase();
        this.answer = answer.toLowerCase();
    }
    
    public LetterResult[] getLetterResults() {
        LetterResult[] results = new LetterResult[5];
        boolean[] used = new boolean[5];
        
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                results[i] = LetterResult.GREEN;
                used[i] = true;
            }
        }
        
        for (int i = 0; i < 5; i++) {
            if (results[i] != LetterResult.GREEN) {
                for (int j = 0; j < 5; j++) {
                    if (!used[j] && guess.charAt(i) == answer.charAt(j)) {
                        results[i] = LetterResult.YELLOW;
                        used[j] = true;
                        break;
                    }
                }
                if (results[i] == null) results[i] = LetterResult.GRAY;
            }
        }
        return results;
    }
}


class GuessResultTest {
    @Test
    void testGetLetterResults() {
        GuessResult result = new GuessResult("brain", "basic");
        assertArrayEquals(new LetterResult[]{LetterResult.GREEN, LetterResult.GRAY, LetterResult.YELLOW, LetterResult.GREEN, LetterResult.GRAY}, result.getLetterResults());
    }
}


class Game {
    private String answer;
    private Dictionary dictionary;
    private int attempts;
    private final int maxAttempts = 6;
    
    public enum GameStatus { PLAYING, WON, LOST }
    private GameStatus status;
    
    public Game(String answer, Dictionary dictionary) {
        this.answer = answer.toLowerCase();
        this.dictionary = dictionary;
        this.status = GameStatus.PLAYING;
        this.attempts = 0;
    }
    
    public GameStatus submitGuess(String guess) throws Exception {
        if (status != GameStatus.PLAYING) throw new Exception("GameAlreadyOverException");
        if (!dictionary.contains(guess)) throw new Exception("IllegalWordException");
        
        attempts++;
        if (guess.equals(answer)) {
            status = GameStatus.WON;
        } else if (attempts >= maxAttempts) {
            status = GameStatus.LOST;
        }
        return status;
    }
    
    public GameStatus getStatus() {
        return status;
    }
}


class GameTest {
    @Test
    void testSubmitGuess() throws Exception {
        Dictionary dict = new Dictionary();
        dict.addWord("apple");
        Game game = new Game("apple", dict);
        
        assertEquals(Game.GameStatus.WON, game.submitGuess("apple"));
        assertThrows(Exception.class, () -> game.submitGuess("wrong"));
    }
}
