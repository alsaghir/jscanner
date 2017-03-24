package com.jscanner.hamada;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws FileNotFoundException {

       //checking the arguments provided when running the application
        if (Argument.checkArguments(args) == 0) {
            System.out.println(Argument.getArgError());
            System.exit(0);
        }

        //Getting the paths of the files if provided
        //Needed for initializing Argument class variables
        Argument.extractPaths(args);

        //Generate tokens from the input file.
        TokenGenerator tokenGenerator = new TokenGenerator(Argument.getSourceFilePath(), Argument.getDestinationFilePath());

        //Out the tokens to the console or to another file
        PrintTokens printTokensToConsole = new PrintTokens(PrintTokens.Output.CONSOLE);
        printTokensToConsole.exportNow();

        PrintTokens printTokensToFile = new PrintTokens(PrintTokens.Output.FILE);
        printTokensToFile.exportNow();

    }
}