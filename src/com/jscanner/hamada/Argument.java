/*
* This is class job is to handle the argument passed as source file path and destination
* file path if provided.
*/

package com.jscanner.hamada;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Argument {

    private static String sourceFilePath;
    private static String destinationFilePath = "";
    private static final String argError = "Wrong arguments provided." +
            "Please provide the path or name of the file containing the code" +
            "with extension .hamada at least to be scanned";

    //getters
    public static String getArgError() {return argError;}
    public static String getSourceFilePath() {return sourceFilePath;}
    public static String getDestinationFilePath() {return destinationFilePath;}


    public static int checkArguments(String[] args) {
        if (args.length >= 1 && args.length <= 2) {
            if (args[0].endsWith(".hamada")) {
                if (args.length == 2) {
                    if (args[1].endsWith(".hamadatokens")) {
                        return 2;
                    } else
                        return 0;
                }
                return 1;
            } else
                return 0;
        } else
            return 0;
    }

    public static void extractPaths(String[] args){
        String[] paths = new String[2];
        System.arraycopy(args, 0, paths, 0, args.length);
        sourceFilePath = args[0];
        if (paths[1] != null){
            destinationFilePath = args[1];
        } else {
            Pattern regex = Pattern.compile(".*(?=\\.hamada)");
            Matcher matcher = regex.matcher(sourceFilePath);
            while(matcher.find())
                destinationFilePath += matcher.group();
            destinationFilePath += ".hamadatokens";
        }
    }
}
