package com.jscanner.hamada;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class TokenGenerator {

    private static final char[] generalLetters = "abcdfghjklmnopqstuwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] kwLetters = "ierv".toCharArray();
    private static final char[] singleSymbols = "+-/,@{}()[];".toCharArray();
    private static final String[] symbolNames = {"plus", "minus", "devision", "colon", "at", "setOpen", "setClose", "parantheseOpen", "parantheseClose", "bracketOpen", "bracketClose", "semiColon"};
    private static final char[] doubleSymbols = "<>=*".toCharArray();
    private static final String[] doubleSingleSymbolNames = {"lessThan", "moreTHan", "equal", "multiply"};
    private static final String[] doubleSymbolsLiterals = {"<=", ">=", "<>", "==", "**"};
    private static final String[] doubleSymbolNames = {"lessThanOrEqual", "moreTHanOrEqual", "notEqual", "equality", "doubleMultiply"};
    private static final char[] spaces = {'\r', '\n', '\t', ' '};
    public static ArrayList<String> tokenClass;
    public static ArrayList<String> tokenLexeme;
    private static String state;
    private static String currentContinuousString;
    private static boolean endOfFile = false;
    private static boolean getNextCharacter = true;
    private static FileReader inputFile;
    private static BufferedReader bufferedReader;
    private static int currentCharacterIntegerForm = Integer.MIN_VALUE;


    public TokenGenerator(String sourceFilePath, String destinationFilePath) {

        //initializing member variables
        state = "initial";
        tokenClass = new ArrayList<String>();
        tokenLexeme = new ArrayList<String>();
        currentContinuousString = "";


        try {//Reading the input file
            inputFile = new FileReader(Argument.getSourceFilePath());
            bufferedReader = new BufferedReader(inputFile);

            //scanning characters
            while (!endOfFile) {
                endOfFile = (currentCharacterIntegerForm = bufferedReader.read()) == -1;
                getNextCharacter = false;
                scanForTokens((char) currentCharacterIntegerForm);
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("No file to read");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Buffer Error");
            e.printStackTrace();
        }
    }

    //handles the FSM usning switch/if statements
    private static void scanForTokens(char currentCharacter) {

        //if the flag is false then continue with this same character
        while (!getNextCharacter) {
            switch (state) {

                case "initial":

                    getNextCharacter = true;
                    currentContinuousString = "";


                    //Identifier so the input is letter or #
                    if (contains(generalLetters, currentCharacter) || currentCharacter == '#') {
                        moveToState("idstate");
                        getNextCharacter = true;
                        currentContinuousString += currentCharacter;
                    } else if (contains(spaces, currentCharacter)) {
                        moveToState("initial");
                        getNextCharacter = true;
                        currentContinuousString = Character.toString(currentCharacter);
                        currentContinuousString = currentContinuousString.replace("\n", "\\n");
                        currentContinuousString = currentContinuousString.replace("\r", "\\r");
                        currentContinuousString = currentContinuousString.replace("\t", "\\t");
                        currentContinuousString = currentContinuousString.replace(" ", "\\s");
                        storeAndClean(Classes.WHITE_SPACE.toString());
                    } else if (currentCharacter == '%') {
                        currentContinuousString = "%";

                        //read until newline, space or tab found. the \r for Windows ONLY
                        try {
                            while (((currentCharacterIntegerForm = bufferedReader.read()) != -1) && ('\n' != (char) currentCharacterIntegerForm) && ('\r' != (char) currentCharacterIntegerForm)) {
                                currentContinuousString += (char) currentCharacterIntegerForm;
                            }
                        } catch (IOException e) {
                            System.out.println("IO EXCEPTION in initial state");
                            e.printStackTrace();
                        }
                        currentContinuousString = currentContinuousString.replace("\n", "\\n");
                        currentContinuousString = currentContinuousString.replace("\r", "\\r");
                        currentContinuousString = currentContinuousString.replace("\t", "\\t");
                        //currentContinuousString = currentContinuousString.replace(" ", "\\s");
                        moveToState("initial");
                        storeAndClean(Classes.Line_COMMENT.toString());
                        currentCharacter = (char) currentCharacterIntegerForm;
                        getNextCharacter = false;
                    } else if (currentCharacter == '\'') {
                        int firstSequencedQuotes = 1;
                        int lastSequencedQuotes = 0;
                        boolean blockComment = false;
                        boolean tempFlag = true;
                        //read until the end or until single quotation mark
                        currentContinuousString = "'";
                        try {
                            if (((currentCharacterIntegerForm = bufferedReader.read()) != -1) && ('\'' == (char) currentCharacterIntegerForm)) {
                                firstSequencedQuotes++;
                                if (((currentCharacterIntegerForm = bufferedReader.read()) != -1) && ('\'' == (char) currentCharacterIntegerForm)) {
                                    firstSequencedQuotes++;
                                    currentContinuousString = "'''";
                                    blockComment = true;
                                    while (((currentCharacterIntegerForm = bufferedReader.read()) != -1) && tempFlag) {
                                        if ('\'' == (char) currentCharacterIntegerForm) {
                                            lastSequencedQuotes++;
                                        } else if ('\'' != (char) currentCharacterIntegerForm) {
                                            lastSequencedQuotes = 0;
                                        }

                                        if (lastSequencedQuotes == 3)
                                            tempFlag = false;

                                        currentContinuousString += (char) currentCharacterIntegerForm;
                                    }
                                }
                            }

                            if (firstSequencedQuotes == 2){
                                currentContinuousString = "''";

                            } else if (!blockComment){
                                currentContinuousString += (char) currentCharacterIntegerForm;
                                while (((currentCharacterIntegerForm = bufferedReader.read()) != -1) && ('\'' != (char) currentCharacterIntegerForm)) {
                                    currentContinuousString += (char) currentCharacterIntegerForm;
                                }
                                currentContinuousString += "'";
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        getNextCharacter = true;
                        moveToState("initial");
                        if (currentContinuousString.length() >= 3 && currentContinuousString.charAt(2) == '\'')
                            storeAndClean(Classes.BLOCK_COMMENT.toString());
                        else
                            storeAndClean(Classes.STRING.toString());

                        currentContinuousString += (char) currentCharacterIntegerForm;

                    } else if (currentCharacter == 'i') {
                        moveToState("ist");
                        getNextCharacter = true;
                        currentContinuousString = Character.toString(currentCharacter);

                    } else if (currentCharacter == 'e') {
                        moveToState("elsest1");
                        getNextCharacter = true;
                        currentContinuousString = Character.toString(currentCharacter);

                    } else if (currentCharacter == 'r') {
                        moveToState("retst1");
                        getNextCharacter = true;
                        currentContinuousString = Character.toString(currentCharacter);

                    } else if (currentCharacter == 'v') {
                        moveToState("voidst1");
                        getNextCharacter = true;
                        currentContinuousString = Character.toString(currentCharacter);

                    } else if (Character.isDigit(currentCharacter)) {
                        try {

                            while (!endOfFile && Character.isDigit(currentCharacter)) {
                                currentContinuousString += (char) currentCharacterIntegerForm;
                                endOfFile = ((currentCharacterIntegerForm = bufferedReader.read()) == -1);
                                currentCharacter = (char) currentCharacterIntegerForm;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        getNextCharacter = false;
                        moveToState("initial");
                        storeAndClean(Classes.NUMBER.toString());

                    } else if (contains(singleSymbols, currentCharacter)) {
                        moveToState("initial");
                        getNextCharacter = true;
                        currentContinuousString = Character.toString(currentCharacter);
                        storeAndClean(Classes.SYMBOL.toString());
                    } else if (contains(doubleSymbols, currentCharacter)) {

                        currentContinuousString = Character.toString(currentCharacter);

                        if (Objects.equals(currentContinuousString, "*")) {
                            currentCharacter = handleSimilarSymbolsInput('*');
                        } else if (Objects.equals(currentContinuousString, "=")) {
                            currentCharacter = handleSimilarSymbolsInput('=');
                        } else if (Objects.equals(currentContinuousString, "<")) {
                            handleLessThanSymbol();
                        } else if (Objects.equals(currentContinuousString, ">")) {
                            try {
                                endOfFile = ((currentCharacterIntegerForm = bufferedReader.read()) == -1);
                                if (('=' == (char) currentCharacterIntegerForm) && !endOfFile) {
                                    currentContinuousString += (char) currentCharacterIntegerForm;
                                    getNextCharacter = true;
                                } else {
                                    getNextCharacter = false;
                                    currentCharacter = (char) currentCharacterIntegerForm;
                                }
                                moveToState("initial");
                                storeAndClean(Classes.SYMBOL.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;

                case "ist":
                    if (currentCharacter == 'n')
                        moveToState("intst1");
                    else if (currentCharacter == 'f')
                        moveToState("ifst");
                    else
                        moveToState("idstate");

                    getNextCharacter = true;
                    currentContinuousString += currentCharacter;
                    break;

                case "ifst":
                    lastKeywordLetterState(currentCharacter);
                    break;

                case "intst1":
                    checkKeywordLetters('t', currentCharacter, "intst2");
                    break;

                case "intst2":
                    lastKeywordLetterState(currentCharacter);
                    break;

                case "elsest1":
                    checkKeywordLetters('l', currentCharacter, "elsest2");
                    break;

                case "elsest2":
                    checkKeywordLetters('s', currentCharacter, "elsest3");
                    break;

                case "elsest3":
                    checkKeywordLetters('e', currentCharacter, "elsest4");
                    break;

                case "elsest4":
                    lastKeywordLetterState(currentCharacter);
                    break;

                case "retst1":
                    checkKeywordLetters('e', currentCharacter, "retst2");
                    break;

                case "retst2":
                    checkKeywordLetters('t', currentCharacter, "retst3");
                    break;

                case "retst3":
                    checkKeywordLetters('u', currentCharacter, "retst4");
                    break;

                case "retst4":
                    checkKeywordLetters('r', currentCharacter, "retst5");
                    break;

                case "retst5":
                    checkKeywordLetters('n', currentCharacter, "retst6");
                    break;

                case "retst6":
                    lastKeywordLetterState(currentCharacter);
                    break;

                case "voidst1":
                    checkKeywordLetters('o', currentCharacter, "voidst2");
                    break;
                case "voidst2":
                    checkKeywordLetters('i', currentCharacter, "voidst3");
                    break;
                case "voidst3":
                    checkKeywordLetters('d', currentCharacter, "voidst4");
                    break;
                case "voidst4":
                    lastKeywordLetterState(currentCharacter);
                    break;

                case "idstate":
                    if (Objects.equals(currentContinuousString, "#")) {
                        currentContinuousString += currentCharacter;
                        getNextCharacter = true;
                    } else if (contains(generalLetters, currentCharacter) || contains(kwLetters, currentCharacter)) {
                        currentContinuousString += currentCharacter;
                        getNextCharacter = true;
                    } else if (currentContinuousString.length() == 1 || ((currentContinuousString.length() == 2) && currentContinuousString.charAt(0) == '#')) {
                        moveToState("initial");
                        getNextCharacter = false;
                        storeAndClean(Classes.ERROR.toString());
                    } else {
                        moveToState("initial");
                        getNextCharacter = false;
                        storeAndClean(Classes.IDENTIFIER.toString());
                    }
                    break;
                default:
                    getNextCharacter = true;
                    currentContinuousString += currentCharacter;
                    storeAndClean(Classes.ERROR.toString());
            }
        }
    }

    private static void moveToState(String st) {
        state = st;
    }

    //store the token as class and lexeme in arraylists and reset the main string varuable
    private static void storeAndClean(String className) {

        //for symbol names
        if (Objects.equals(className, Classes.SYMBOL.toString())) {
            if (contains(singleSymbols, currentContinuousString.charAt(0))) {
                for (int i = 0; i < singleSymbols.length; i++) {
                    if (Objects.equals(currentContinuousString, Character.toString(singleSymbols[i]))) {
                        tokenLexeme.add(currentContinuousString);
                        tokenClass.add(symbolNames[i]);
                    }
                }
            } else if (contains(doubleSymbols, currentContinuousString.charAt(0))) { // for possible double symbols
                if (currentContinuousString.length() == 2) {
                    for (int i = 0; i < doubleSymbolNames.length; i++) {
                        if (Objects.equals(currentContinuousString, doubleSymbolsLiterals[i])) {
                            tokenLexeme.add(currentContinuousString);
                            tokenClass.add(doubleSymbolNames[i]);
                        }
                    }
                } else if (currentContinuousString.length() == 1) {
                    for (int i = 0; i < doubleSingleSymbolNames.length; i++) {
                        if (Objects.equals(currentContinuousString, Character.toString(doubleSymbols[i]))) {
                            tokenLexeme.add(currentContinuousString);
                            tokenClass.add(doubleSingleSymbolNames[i]);
                        }
                    }
                }
            }

            } else {
                tokenLexeme.add(currentContinuousString);
                tokenClass.add(className);
            }
            currentContinuousString = "";
        }

    private static char handleSimilarSymbolsInput(char symbol) {
        char tempCurrentChar;
        try {
            endOfFile = ((currentCharacterIntegerForm = bufferedReader.read()) == -1);
            if ((symbol == (char) currentCharacterIntegerForm) && !endOfFile) {
                currentContinuousString += (char) currentCharacterIntegerForm;
                getNextCharacter = true;
            } else {
                getNextCharacter = false;

            }
            moveToState("initial");
            storeAndClean(Classes.SYMBOL.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempCurrentChar = (char) currentCharacterIntegerForm;
        return tempCurrentChar;
    }

    private static void handleLessThanSymbol() {
        try {
            endOfFile = ((currentCharacterIntegerForm = bufferedReader.read()) == -1);
            if ((('=' == (char) currentCharacterIntegerForm) || ('>' == (char) currentCharacterIntegerForm)) && !endOfFile) {
                currentContinuousString += (char) currentCharacterIntegerForm;
                getNextCharacter = true;
            } else {
                getNextCharacter = false;
            }
            moveToState("initial");
            storeAndClean(Classes.SYMBOL.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean contains(char[] charArray, char c) {
        boolean contains = false;
        for (char temp : charArray) {
            if (temp == c) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private static void checkKeywordLetters(char conditionCharacter, char currentChecknigCharacter, String newState) {

        if (conditionCharacter == currentChecknigCharacter) {
            moveToState(newState);
            getNextCharacter = true;
            currentContinuousString += currentChecknigCharacter;
        } else if (contains(generalLetters, currentChecknigCharacter) || (contains(kwLetters, currentChecknigCharacter))) {
            moveToState("idstate");
            getNextCharacter = false;
        } else {
            moveToState("initial");
            getNextCharacter = false;
            storeAndClean(Classes.IDENTIFIER.toString());
        }

    }

    private static void lastKeywordLetterState(char conditionCharacter) {

        if (contains(generalLetters, conditionCharacter) || (contains(kwLetters, conditionCharacter))) {
            moveToState("idstate");
            getNextCharacter = true;
            currentContinuousString += conditionCharacter;
        } else {
            moveToState("initial");
            getNextCharacter = false;
            storeAndClean(Classes.KEYWORD.toString());
        }

    }

    public enum Classes {
        IDENTIFIER("Id"), NUMBER("num"), STRING("string"), SYMBOL("symbol"), WHITE_SPACE("blank"), BLOCK_COMMENT("block"), Line_COMMENT("line"), ERROR("error"), KEYWORD("keyword");
        private final String text;

        private Classes(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}