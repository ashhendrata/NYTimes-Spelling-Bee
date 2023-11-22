import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;
import java.io.*;

//Handles the logic for the JavaBee game
public class GameLogic{
   
   
   //Name of dictionary file (containing English words to validate guesses against)
   private static final String DICTIONARY_FILENAME = "dictionary.txt";   
    
   //Total number of hives in the game
   public static final int HIVE_COUNT = 7;
      
   //Required Min/Max length for a valid player guess
   public static final int MIN_WORD_LENGTH = 4; 
   public static final int MAX_WORD_LENGTH = 19;    
   
   
   //Required Min/Max number of formable words for a randomized hive
   public static final int MIN_FORMABLE = 30;
   public static final int MAX_FORMABLE = 110;    
   
   //Collection of various letters (vowels only, consonants only, all letters)
   public static final String VOWEL_CHARS = "AEIOU";  
   public static final String CONSONANT_CHARS = "BCDFGHJKLMNPQRSTVWXYZ";
   public static final String ALL_CHARS = VOWEL_CHARS + CONSONANT_CHARS;
   
   //The various score rank thresholds and their respective titles
   public static final double[] RANK_PERCENTS = {0, 0.02, 0.05, 0.08, 0.15, 0.25, 0.4, 0.5, 0.7};
   public static final String[] RANK_TITLES = {"Beginner", "Good Start", "Moving Up", 
      "Good", "Solid", "Nice", "Great", 
      "Amazing", "Genius"};
   
   //Text for different error messages that occur for various invalid inputs
   private static final String ERROR_TOO_LONG = "Too long...";
   private static final String ERROR_TOO_SHORT = "Too short...";
   private static final String ERROR_MISSING_CENTER = "Missing yellow letter...";
   private static final String ERROR_INVALID_LETTER = "Contains non-hive letter...";   
   private static final String ERROR_ALREADY_FOUND = "Already in word list...";  
   private static final String ERROR_NOT_WORD = "Not in dictionary...";
   
   //Character codes for the enter and backspace key press
   public static final char ENTER_KEY = KeyEvent.VK_ENTER;
   public static final char BACKSPACE_KEY = KeyEvent.VK_BACK_SPACE;  
   
   //A collection of letters to be used for the hives when the "Hardcoded hives" debug is enabled
   private static final char[] DEBUG_HARDCODED_HIVES = {'C', 'O', 'L', 'G', 'A', 'T', 'E'};      
   
   //Use me for generating random numbers (see https://docs.oracle.com/javase/8/docs/api/java/util/Random.html)!
   private static final Random rand = new Random(); 


   
   
   //******************   NON-FINAL GLOBAL VARIABLES   ******************
   
   
   //Array storing all formable words given the chosen hives
   public static String[] validWords = new String[MAX_FORMABLE];   
   
   //The maximum number of points possible given the game's chosen hive letters
   public static int maxPoints = 0;
   
   
   
   //*******************************************************************
   
   
   //This function gets called ONCE when the game is very first launched
   //before the user has the opportunity to do anything.
   //
   //Should perform any initialization that needs to happen at the start of the game,
   //and return the randomly chosen hive letters as a char array.  Whichever letter
   //is at index 0 of the array will be the center (yellow) hive letter, the remainder
   //will be the outer (gray) hive letters.
   //
   //The returned char array:
   //  -must be seven letters long
   //  -cannot have duplicate letters
   //  -cannot have an 'S' as one of its letters
   //  -must contain AT LEAST one vowel character (AEIOU) 
   //   (additionally: if the array only contains one vowel, it should be 
   //    possible for the vowel to be in any hive, including the center)
   public static char[] initializeGame(){
      GameGUI.setRank(RANK_TITLES[0]); //Rank is Beginner when the game first begins
      if (JavaBeeLauncher.DEBUG_USE_HARDCODED_HIVES){
         countFormableWords(DEBUG_HARDCODED_HIVES); //adds words to validWords array for guess validation and setting rank HOWEVER, no need to assign to variable because no need for count (the hard coded hives are guarenteed to satisfy the min and max formable words condition)
         return DEBUG_HARDCODED_HIVES;
      } else {
         char[] newHive = new char[HIVE_COUNT]; 
         while (true){ //true refers to the fact that a valid hive that fulfills all the above conditions has yet to be created
            Arrays.fill(validWords, null); //"Clears" validWords for new set of words made from new set of hive letters
            Arrays.fill(newHive, '\u0000'); //"Restarts" hive
            newHive = insertVowels(newHive); 
            newHive = insertConsonants(newHive);
            int numFormableWords = countFormableWords(newHive); //Counts the number of formable words added to validWords array
            if (numFormableWords >= MIN_FORMABLE && numFormableWords <= MAX_FORMABLE){  //Hive letters should only be able to make a certain number of valid words
               break;
            }
         }
         return newHive;
      }
   }

   //******************  THE FOLLOWING HELPER FUNCTIONS HELP BREAK DOWN initializeGame(), AND THUS LARGELY INVOLVE THE SELECTION OF HIVE LETTERS  ******************

   //Selects random number of vowels and random vowels to place in random hives (random indices)
   public static char[] insertVowels(char[] newHive){
      String cumVowels = ""; //Helps keep track of and prevent duplicates
      int numVowels = 1 + rand.nextInt(VOWEL_CHARS.length()); //Produces a random number from 1 to 5 (Hive has to have at least one vowel and max of 5)
      for (int i = 0; i <= numVowels; i++){
         int randHiveIndex = rand.nextInt(HIVE_COUNT); //Produces a number from 0 to 6 (representing index of newHive)
         if (newHive[randHiveIndex] == '\u0000'){ //If it is not filled up by a letter yet
            int randVowelIndex = rand.nextInt(VOWEL_CHARS.length()); //Produces random index of VOWEL_CHARS, effectively choosing a random vowel
            if (!cumVowels.contains(String.valueOf(VOWEL_CHARS.charAt(randVowelIndex)))){ //If the chosen vowel already exists in newHive, it won't be added, Regardless, at least one vowel would be added to a hive since the first vowel will definitely not have duplicates
               newHive[randHiveIndex] = VOWEL_CHARS.charAt(randVowelIndex); 
               cumVowels += VOWEL_CHARS.charAt(randVowelIndex); 
            }
         }
      }
      return newHive;
   }

   //Selects random consonants to insert in remaining hives (remaining indicies)
   public static char[] insertConsonants(char[] newHive){
      String cumConsonants = ""; //Helps keep track of and prevent duplicates
      for (int i = 0; i < newHive.length; i++){
         if (newHive[i] == '\u0000'){  //If not filled up by letter yet
            while (true){ //true refers to the fact that a unique consonant has yet to be found
               int randConIndex = rand.nextInt(CONSONANT_CHARS.length()); //Produces random index of CONSONANT_CHARS effectively choosing a random consonant
               if (!cumConsonants.contains(String.valueOf(CONSONANT_CHARS.charAt(randConIndex)))){ //No duplicates
                  if (CONSONANT_CHARS.charAt(randConIndex) != 'S'){ //Hives cannot have the character 'S'
                     newHive[i] = CONSONANT_CHARS.charAt(randConIndex); 
                     cumConsonants += CONSONANT_CHARS.charAt(randConIndex);
                     break;
                  }
               }
            }
         }
      }
      return newHive;
   }

   //Counts number of formable words that can be made using newHive letters
   //Stores formable words in validWords global array
   //Returns -1 if there are more than 110 formable words
   public static int countFormableWords(char[] newHive){
      Scanner scan;
      File validWordsFile = new File(DICTIONARY_FILENAME);
      try{
         int formableCount = 0;
         scan = new Scanner(validWordsFile);
         while (scan.hasNextLine()){
            String validEnglishWord = scan.nextLine();
            boolean madeOfHiveLetters = allLettersInHive(validEnglishWord, newHive); //a formable word must be made up of only hive letters
            if (madeOfHiveLetters && validEnglishWord.contains(String.valueOf(newHive[0]))){ //Has to have yellow hive letter (newHive[0] is the yellow hive letter)
               if (validEnglishWord.length() >= MIN_WORD_LENGTH && validEnglishWord.length() <= MAX_WORD_LENGTH){ //Has to be at least 4 letters long and no more than 19
                     formableCount++; //a formable word has only hive letters, at least one yellow hive letter, and has a length between 4 to 19 (inclusive)
                     if (formableCount > MAX_FORMABLE){ //Prevents an index out of bounds error (array won't be overfilled)
                        scan.close();
                        return -1;
                     }
                     validWords[formableCount-1] = validEnglishWord; //adds formable word to the validWords global array
               }
            }
         }
         scan.close();
         return formableCount;
      } 
      catch (FileNotFoundException fnfe){
         System.out.print("File cannot be found...");
         System.exit(1);
         return -1;
      }
   }

   //*******************************************************************

   //This function gets called ONCE after the graphics window has been
   //initialized and initializeGame has been called.
   public static void warmup(){
      /*
      GameGUI.setPlayerGuess("ASHLEY");
      GameGUI.setRank("CompSci");
      GameGUI.addToWordList("Raiders", 100);
      GameGUI.addToWordList("Java", 2);
      GameGUI.getPlayerScore();
      */
      

   }     
   
   



   //I took into consideration a total of 6 conditions:
   //1. word has a max of 19 characters
   //2. word has at least 4 characters
   //3. word is made up of only hive letters
   //4. at least one letter is a yellow hive letter
   //5. no repeats (cant be from word list)
   //6. must be a valid english word (be in dictionary.txt)

   //This function gets called everytime the user types a valid key on the
   //keyboard (alphabetic character, enter, or backspace) or clicks one of the
   //hives/buttons in the game window.
   //The key pressed is passed in as a char value.
   public static void reactToKey(char key){
      /* WarmUp:
      if (key == 'G'){
         GameGUI.displayErrorMessage("WARMUP!");
         GameGUI.wigglePlayerGuess();
      }
      */
      String playerGuess = GameGUI.getPlayerGuessStr();
      if (Character.isAlphabetic(key)){ //Only alphabets can be typed into the player guess area
         if (playerGuess.length() < MAX_WORD_LENGTH){ //First condition: word has a maximum of 19 characters
            playerGuess = playerGuess + key; //Allows users to type their input into the player guess area
         } else {
            errorFound(ERROR_TOO_LONG); //User tries to type in a word with more than 19 leters
         }
      } else if (key == BACKSPACE_KEY){ 
         if (!playerGuess.isEmpty()){ //prevents an index out of bounds error
            playerGuess = playerGuess.substring(0, playerGuess.length()-1); //Allows users to delete end characters from input
         }
      } else if (key == ENTER_KEY){ //User tries to validate guess
         if (playerGuess.length() < MIN_WORD_LENGTH){ 
            errorFound(ERROR_TOO_SHORT); //Second condition: Guess must be at least 4 letters
         } else if (!allLettersInHive(playerGuess, GameGUI.getAllHiveLetters())){
            errorFound(ERROR_INVALID_LETTER); //Third condition: all letters in playerGuess must be hive letters
         } else if (!yellowHiveFound(playerGuess)){
            errorFound(ERROR_MISSING_CENTER); //Fourth condition: at least one letter in playerGuess must be a letter from the yellow hive
         } else if (isInWordList(playerGuess)) {
            errorFound(ERROR_ALREADY_FOUND); //Fifth condition: the entered word cannot already have been entered (no duplicates meaning cannot be in word list)
         } else if (!isValidWord(playerGuess)){
            errorFound(ERROR_NOT_WORD); //Sixth condition: Must be a valid English word (in the dictionary file)
         } else {
            int playerScore = scoreObtained(playerGuess); //Calculates the score obtained for the specific entered word
            GameGUI.addToWordList(playerGuess, playerScore); //Adds word to word list with score calculated using the function called above
            GameGUI.setRank(playerRank());
            playerGuess = ""; //Player text area is cleared
         }   
      }
         GameGUI.setPlayerGuess(playerGuess); //Sets the player guess text in the game window to playerGuess
         System.out.println("reactToKey(...) called! key (int value) = '" + ((int)key) + "'");
   }
   
   //*******  THE FOLLOWING HELPER FUNCTIONS HELP BREAK DOWN THE reactToKey(char key) FUNCTION  *****************************************
   //*******  THIS MEANS THEY LARGELY REVOLVE AROUND VALIDATING THE PLAYER'S GUESS, CALCULATING SCORES, AND SETTING RANKS, etc.  ********

   //Displays the suitable error message and wiggles the player guess area
   public static void errorFound(String errorMessage){
      GameGUI.wigglePlayerGuess();;
      GameGUI.displayErrorMessage(errorMessage);
   }

   //Checks if entered guess is in the validWords global array
   //Helps with checking condition 6: word must be a valid english word (found in dictionary file)
   public static boolean isValidWord(String playerGuess){
      if (JavaBeeLauncher.DEBUG_NO_DICT_VERIFY){ //No dictionary check
         return true;
      } else {
         for (int i = 0; i < validWords.length && validWords[i] != null; i++){ //Runs through array (only the indices with words)
            if (playerGuess.equals(validWords[i])){
               return true; //player guess is a valid word
            }
         }
         return false; //not found in validWords array
      }
   }

   //Returns true if Yellow hive letter exists in player guess
   public static boolean yellowHiveFound(String playerGuess){
      for (int i = 0; i < playerGuess.length(); i++){
         if (playerGuess.charAt(i) == GameGUI.getCenterHiveLetter()){
            return true; //At least one letter must be from center
         }
      }
      return false;
   }

   //Checks for duplicates, which means it returns true if playerGuess already exists in wordList
   public static boolean isInWordList(String playerGuess){
      String[] wordList = GameGUI.getWordList();
      for (String word : wordList) { //Iterates through word list
         if (word.toUpperCase().equals(playerGuess.toUpperCase())) { //Compares the words in word list to the player guess
            return true; //it is a duplicate
         }
      }
      return false;
   }

   
   //Used to find out if player guess is made up of letters only from the hives
   public static boolean allLettersInHive(String playerGuess, char[] hiveLetters){
      playerGuess = playerGuess.toUpperCase();
      for (char inputLetter: playerGuess.toCharArray()){ //Iterates over every letter of player guess
         boolean letterFound = false;
         for (char hiveLetter: hiveLetters){ //Iterates over every character in an array of hive characters
            if (inputLetter == hiveLetter){ //Compares each input character to each hive letter until a match is found
               letterFound = true;
               break;
            }
         }
         if (letterFound == false){ //The loop has ran through all the letters of the array of hive characters and no match is found
            return false;
         }
      }
      return true;
   }  

   //Calculates score of each word
   public static int scoreObtained(String playerGuess){
      int playerScore = 0;
      if (playerGuess.length() == 4){ //criteria 1: words four characters long are worth one point
         playerScore += 1;
         }
      if (playerGuess.length() > 4) { //criteria 2: words longer than 4 characters are worth one point per letter, including dupicates
         playerScore += playerGuess.length();
      } 
      boolean extraSeven = allSevenHives(playerGuess);
      if (extraSeven) { //criteria 3: words using all seven hive letters are worth an extra 7 points in addition to the above (criteria 2)
         playerScore += 7;
      }
      return playerScore;
   }

   //Used to find out if all 7 hive letters are used (this means there has to be 7 different letters)
   public static boolean allSevenHives(String playerGuess){ 
      String cumHiveLetters = ""; //unique letters
      for (int i = 0; i < playerGuess.length(); i++){
         if (!cumHiveLetters.contains(String.valueOf(playerGuess.charAt(i)))){ //if character not found in guess yet while iterating, it will add onto the cumHiveLetters
            cumHiveLetters += String.valueOf(playerGuess.charAt(i));
         }
      }
      if (cumHiveLetters.length() == HIVE_COUNT){ //must have 7 different letters
         return true;
      } else {
         return false;
      }


   }
   //Finds the maximum number of possible points
   public static int findMaxPoints(String[] validWords){
      int max = 0;
      for (int i = 0; i < validWords.length; i++){
         if (validWords[i] != null){
            max += scoreObtained(validWords[i]); //This calls the helper function that helps calculate the number of points won for each word
         }
      }
      return max;
   }
   
   //Finds out the player's rank by caluclating how it compares to the max number of possible points
   public static String playerRank(){
      String rank = RANK_TITLES[0]; //starts with Beginner
      maxPoints = findMaxPoints(validWords);
      int totalScore = GameGUI.getPlayerScore(); //total score obtained by player so far
      double currentPercent = (double) totalScore / (double) maxPoints; //The player’s rank is determined by what percentage of the total possible points they’ve accrued
      for (int i = RANK_PERCENTS.length - 1; i >= 0; i--){ //iterates through rank percents from biggest to smallest
         if (currentPercent >= RANK_PERCENTS[i]){ //e.g., if it is more than or equal to 70% (0.7), rank will be Genius
            rank = RANK_TITLES[i]; //assigns rank to a rank title 
            break;
         }
      }
      return rank;
      
   }

}
