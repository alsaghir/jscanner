package com.jscanner.hamada;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PrintTokens {

    private String outputString = "";
    private static FileWriter targetFile;
    private static BufferedWriter bufferedWriter;

    public enum Output{
        CONSOLE, FILE
    }

    private Output out;

    public PrintTokens(Output out){
        this.out = out;
        createOutputString();
    }

    public void exportNow(){
        if(out == Output.CONSOLE){
            exportToConsole();
        } else {
            exportToFile();
        }
    }

    private String createOutputString(){
        for(int i =0 ; i < TokenGenerator.tokenLexeme.size(); i++){
            outputString += "<" + TokenGenerator.tokenClass.get(i) + ", " + TokenGenerator.tokenLexeme.get(i) + ">" + System.lineSeparator();
        }

        return outputString;
    }

    private void exportToConsole(){
            System.out.println(outputString);
    }

    private void exportToFile(){
        try {
            targetFile = new FileWriter(Argument.getDestinationFilePath());
            bufferedWriter = new BufferedWriter(targetFile);
            bufferedWriter.write(outputString);
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Buffer Error");
            e.printStackTrace();
        }
    }


}
