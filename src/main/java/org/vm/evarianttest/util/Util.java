package org.vm.evarianttest.util;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class.
 *
 * @author vivekm
 * @since 1.0
 */
public class Util {
    /**
     * This method returns a string separated by comma and wrapped by [] brackets, by concatenating the input objects.
     * example output: [1,2,3]
     *
     * This method uses String.value(o) to get the String value.
     *
     * @param input - Collection of objects
     * @return  - String
     */
    public static String listToString(Collection<? extends Object> input){
        StringBuilder retValue = new StringBuilder("[");
        boolean isFirst = true;
        for(Object o : input){
            retValue.append(isFirst ? "" : ",").append(String.valueOf(o));
            isFirst = false;
        }
        retValue.append("]");
        return retValue.toString();
    }

    /**
     * This method validates the input Path and returns appropriate messages.
     *
     * @param path
     * @return StringBuilder with errors if any.
     */
    public static StringBuilder validateInput(Path path) {
        StringBuilder sbr = new StringBuilder();
        if(path == null){
            sbr.append("Input file URI cannot be null\n");
        }else{
            File file = path.toFile();
            if(!file.exists()){
                sbr.append("Input File ").append(path.toAbsolutePath()).append("does not exist\n");
            }
            if(!file.isFile()){
                sbr.append("Input URI ").append(path.toAbsolutePath()).append(" is not a valid File\n");
            }
            if(!file.canRead()){
                sbr.append("Input URI ").append(path.toAbsolutePath()).append(" is not readable\n");
            }
        }
        return sbr;
    }

    /**
     * This method returns the list of State names from the input string, which is of the following format: austin-roundrock,tx.
     *
     * @param name
     * @return
     */
    public static List<String> getCleanStateNames(String name){
        return cleanNames(name, 1);
    }

    /**
     * This method returns the list of City names from the input string, which is of the following format: austin-roundrock,tx.
     *
     * @param name
     * @return
     */
    public static List<String> getCleanCityNames(String name){
        return cleanNames(name, 0);
    }

    private static List<String> cleanNames(String name, int section) {
        if(name == null || name.isEmpty())
            return null;
        String[] inputArr = name.split(",");
        List<String> cleanedNames = new LinkedList<>();
        for(String s : inputArr[section].split("-")){
            cleanedNames.add(cleanString(s));
        }
        return cleanedNames;
    }

    /**
     * This method creates a Statistical Area Key String of format AUSTIN-ROUNDROCK,TX.
     *
     * @param extractedCityNames - List of City names
     * @param extractedStateNames - List of State names.
     * @return String
     */
    public static String makeKey(List<String> extractedCityNames, List<String> extractedStateNames) {
        StringBuilder sbr = new StringBuilder();

        extractedCityNames.forEach( s -> sbr.append(s).append("-"));
        sbr.deleteCharAt(sbr.length()-1);
        sbr.append(",");
        extractedStateNames.forEach( s -> sbr.append(s).append("-"));
        sbr.deleteCharAt(sbr.length()-1);

        return sbr.toString();
    }

    /**
     * This method cleans a given String by removing any padding blank spaces, double quotes and converts into upper case.
     *
     * @param input
     * @return
     */
    public static String cleanString(String input){
        if(input == null)
            return input;
        return input.trim().replace("\"", "").toUpperCase(Locale.getDefault());
    }

    /***
     * This method returns a cleaned Statistical Area name, by applying cleanString() method.
     *
     * @param input
     * @return
     */
    public static String cleanSAName(String input){
        if(input == null || input.isEmpty())
            return null;
        String[] inputArr = input.split(",");
        StringBuilder sbr = new StringBuilder();

        for(String s : inputArr[0].split("-"))
            sbr.append(cleanString(s)).append("-");
        sbr.deleteCharAt(sbr.length()-1).append(",");

        for(String s : inputArr[1].split("-"))
            sbr.append(cleanString(s)).append("-");
        sbr.deleteCharAt(sbr.length() - 1);
        return sbr.toString();
    }

    /**
     * This method splits the input into an array using pipe as delimter.
     *
     * @param input
     * @return
     */
    public static String[] splitPSVLine(String input){
        if(input == null)
            return null;
        //String.split expects a regular expression argument. An unescaped | is parsed as a regex meaning "empty string or empty string,", hence we need to escape it.
        return input.trim().split("\\|");
    }

    /**
     * This method returns a Data by parsing the input string using the input format. It eats any exceptions in the process and returns null, if so.
     *
     * @param yearMonthDay
     * @param inputDatePattern
     * @return Date instance
     */
    public static Date safeParseDate(String yearMonthDay, String inputDatePattern) {
        DateFormat fmt = new SimpleDateFormat(inputDatePattern);
        Date date = null;
        try{
            date = fmt.parse(yearMonthDay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * This method pad with " " to the right to the given length (n)
     * @param s
     * @param n
     * @return
     */
    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    /**
     * This method pads with " " to the left to the given length (n)
     *
     * @param s
     * @param n
     * @return
     */
    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }
}
