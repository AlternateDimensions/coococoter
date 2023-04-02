import java.io.InvalidObjectException;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    /* Variables */
    private static String separatorOpenings = "([{";
    private static String separatorClosings = ")]}";
    private static String separators = separatorClosings + separatorOpenings;

    private static String FE = "!^";
    private static String MMD = "*%/";
    private static String AS = "+-";
    private static String operators = FE+MMD+AS;

    private static String whitelist = operators + separators + ".";

    private static int lines = 0;
    private static int lineMax = 2147483646; // this helps me avoid those pesky overflow errors

    private static String[] warnings = new String[]{
        "NOTE: Negative numbers are NOT supported. There is no workaround.",
        "NOTE: Distributive multiplcation is not supported. Please add a multiplication symbol between parenthetical and non-parenthetical values*",
        "NOTE: Factorials OF or Powers OF/TO a decimal are NOT supported!",
        "TIP: To clear output history, enter \"clear\"",
        "NOTE: Unclosed parentheses are NOT supported as of 3/31/2023",
        "NOTE: Factorials above 10! are not supported due to constraints."
    };

    /* main is the one that runs. and it sure does run */
    public static void main(String[] args){
        // Initital console clean
        System.out.println("\033[2J");
        System.out.flush(); 

        // Scanner
        Scanner in = new Scanner(System.in);

        // Calculator loop
        while (true){
            // Get input
            String inputString = inputHandler(in);

            // Solve & Print Equation solution
            Object answer;
            try {
                answer = calcHandler(inputString);
                System.out.println("------------------------------------------------------------------------");
                System.out.print("\n".repeat(lines));
                System.out.println("\033[2K\"" + Colors.CYAN + Colors.BOLD + inputString + Colors.DEFAULT + "\" = " + Colors.GREEN + answer + "\n\n\n" + Colors.CLEAR);
                lines = (lines>=lineMax) ? 0 : lines+1;
            } catch (Exception e){
                answer = "An error occurred.";
                System.out.println("------------------------------------------------------------------------");
                System.out.print("\n".repeat(lines));
                System.out.println("\033[2K\"" + Colors.CYAN + Colors.BOLD + inputString + Colors.DEFAULT + "\" = " + Colors.YELLOW + answer + "\n\n\n" + Colors.CLEAR);
            }
        }
    }
    
    /* inputHandler collects and verifies the input to make sure that the user gets the right error.*/
    private static String inputHandler(Scanner in){
        // Variables
        boolean valid = false;
        String input = "";

        // Actual Check
        while (!valid) {
            System.out.print("\033[H"); // Clear each line
            for (String s : warnings){
                System.out.println("\033[2K*"+Colors.ITALIC+s+"*");
            }
            System.out.print("\033[2K"+Colors.CLEAR+Colors.BOLD + "\nInput equation: " + Colors.CYAN + Colors.UNDERLINE);
            input = in.nextLine().replaceAll("\\s", ""); // actually getting the input
            System.out.print(Colors.CLEAR);
            // Check input for non-whitelisted characters
            try{
                // Check if input is empty
                if (input.equals("") || input.equals(null)){
                    throw new Exception("This is an empty equation!");
                }

                for (Character c : input.toCharArray()){ // For each character
                    if (!whitelist.contains(c.toString()) && !Character.isDigit(c)){ // Check if character is in whitelist
                        throw new InvalidObjectException("\""+c.toString()+"\" is not a whitelisted character!"); // breaks loop to show that an error occurred
                    }
                }
                valid = true;
            } catch (Exception e){
                // Input was invalid
                System.out.println("------------------------------------------------------------------------");
                System.out.print("\n".repeat(lines));
                System.out.println("\033[2K\"" + Colors.CYAN + Colors.BOLD + input + Colors.DEFAULT + "\" = " + Colors.RED + e.getMessage() + "\n\n\n" + Colors.CLEAR);
                if (input.equals("clear")){System.out.print("\033[H\033[2J"); lines = 0;}
            }
        }
        return input;
    }

    /* calcHandler deals with different segments' business */
    private static double calcHandler(String equation){
        // Variables
        boolean hasSeparators = false;

        // Separator Check
        for (Character c : separators.toCharArray()){ // For each character in separators string
            if (equation.contains(c.toString())){ // equation input has separator
                hasSeparators = true;
                break; //if any separator is detected, immediately move on.
            }
        }
        /** Due to the setup, hasSeparators will be false if no separators are found in the check **/

        // Evaluation Split
        if (hasSeparators){
            return segmentHandler(equation); // gets the equation via separator handling
        }
        return evaluate(equation); // gets the equation by directly processing it
    }
    
    /* segmentHandler creates segments and runs the recursion setup for each sgement. Note that each execution of segmentHandler occurs at the layer below the previous execution */
    private static double segmentHandler(String equation){
        // Variables
        double answer = 0.0;
        ArrayList<Segment> segments = new ArrayList<Segment>();
        int startIndex = 0, endIndex = 0; // needed to create the segment from scratch
        int sepType = Separators.NONE, sepTypeNeeded = Separators.NONE;  
        int unclosedSegment = 0;

        // Segment Creation

        // Frank's insanity, condensed
        // 1. String is checked LEFT TO RIGHT
        // 2. IF an open bracket is found: Layer = x
        // 3. Continue looking for a closing bracket on layer x or until end of the equation
        // 4. Each open bracket is layer + 1
        // 5. each close bracket is layer -1 
        // 6. Restart the process after the segment has been made!
        // that's the segment process!

        // Theoretically, since the segmentHandler takes care of any segments based on the opening of the brackets, if ONLY the close segment is discovered, it's automatically disregarded as the bracket would contain the entire sentence.
        // If that's undesired, properly close parentheses!!

        for (int i = 0; i < equation.length(); i++){
            // Detect if the character is a separator.
            switch (equation.substring(i,i+1)){
                case ("("): sepType = Separators.OPENCURVE;
                    break;
                case (")"): sepType = Separators.CLOSECURVE;
                    break;
                case ("["): sepType = Separators.OPENSQUARE;
                    break; 
                case ("]"): sepType = Separators.CLOSESQUARE;
                    break;
                case ("{"): sepType = Separators.OPENCURLY;
                    break;
                case ("}"): sepType = Separators.CLOSECURLY;
                    break;
                default: sepType = Separators.NONE;
            }
            // Check Value
            if (sepType < Separators.THRESHOLD){ // check if value is an opening
                if (sepTypeNeeded == Separators.NONE){ // No segment being made right now, let's start to make one
                    sepTypeNeeded = sepType + Separators.THRESHOLD; // saved the separator type
                    unclosedSegment++; // of the bracket type, we have an unclosed segment piece.
                    startIndex = i; // saved the initial index for this segment
                } else if (sepType == sepTypeNeeded - Separators.THRESHOLD) { // same type of separator as segment starter, needs to be accounted for
                    unclosedSegment++;
                }
            } else if (sepType < Separators.NONE && sepType == sepTypeNeeded){ // Not none, but since it comes after the opening check, it will all be closings
                if (unclosedSegment == 1){ // Segment closed
                    endIndex = i;
                    segments.add(new Segment(equation.substring(startIndex+1, endIndex), startIndex, endIndex)); // add new segment
                    startIndex = 0; endIndex = 0;
                }
                unclosedSegment--; // this goes down regardless if it closes a segment. This means it can be negative, but if it does, that just gets removed after all segments are processed.
            }
        }

        if (segments.size() == 0){ // this is here in case a segment isn't made, typically because there was no closing brackets
            segments.add(new Segment(equation.substring(startIndex+1, equation.length()).replaceAll("\\)", "").replaceAll("\\]", "").replaceAll("\\}", ""), startIndex, equation.length()));
        }
        // iterate each segment to get answers and simplify

        for (Segment s : segments){
            // get answer of segment
            answer = calcHandler(s.segment);
            // replace entire segment substring with answer
            equation = equation.substring(0, s.indexStart)+answer+equation.substring(s.indexEnd+1);
            }      

        // Supposedly once the segments run out, they should just return the normal answer.
        answer = calcHandler(equation);
        return answer;
    }

    /* evaluation actually evaluates equations, when broken down, and returns the resulting value */
    private static double evaluate(String equation){
        // Split into string[]
        ArrayList<String> equationFragments = createFragments(equation);
        double answer = 0;

        // Calculate answer
        while (equationFragments.size() > 1){ // this means that there's something to calculate. so do it
        /* These calculations actually removes the fragments with values and replaces the operator fragment with the answer. thus, we merge 3 (or 2) into 1 */
    
            // F - Factorials
            for (int i = 0; i < equationFragments.size(); i++){
                if (equationFragments.get(i).equals("!")){
                    String tempAnswer = String.valueOf(factorial(Double.parseDouble(equationFragments.get(i-1))));
                    equationFragments.set(i, tempAnswer);
                    equationFragments.remove(i-1);
                }
            }

            // E - Exponents
            for (int i = 0; i < equationFragments.size(); i++){
                if (equationFragments.get(i).equals("^")){
                    String tempAnswer = String.valueOf(toPower(Double.parseDouble(equationFragments.get(i-1)), Double.parseDouble(equationFragments.get(i+1))));
                    equationFragments.set(i, tempAnswer);
                    equationFragments.remove(i+1);
                    equationFragments.remove(i-1);
                }
            }
            
            // MMD - Multiply, Modulo, Divide
            for (int i = 0; i < equationFragments.size(); i++){
                String tempAnswer = "";
                switch (equationFragments.get(i)){
                    case ("*"): // multiply
                        tempAnswer = String.valueOf(multiply(Double.parseDouble(equationFragments.get(i-1)), Double.parseDouble(equationFragments.get(i+1))));
                        equationFragments.set(i, tempAnswer);
                        equationFragments.remove(i+1);
                        equationFragments.remove(i-1);
                        break;
                    case ("%"): // modulo, or remainder division
                        tempAnswer = String.valueOf(modulo(Double.parseDouble(equationFragments.get(i-1)), Double.parseDouble(equationFragments.get(i+1))));
                        equationFragments.set(i, tempAnswer);
                        equationFragments.remove(i+1);
                        equationFragments.remove(i-1);
                        break;
                    case ("/"): // division, or actual division
                        tempAnswer = String.valueOf(divide(Double.parseDouble(equationFragments.get(i-1)), Double.parseDouble(equationFragments.get(i+1))));
                        equationFragments.set(i, tempAnswer);
                        equationFragments.remove(i+1);
                        equationFragments.remove(i-1);
                }
            }
            
            // AS - Add, Subtract
            for (int i = 0; i < equationFragments.size(); i++){
                String tempAnswer = "";
                switch (equationFragments.get(i)){
                    case ("+"): // addition
                        tempAnswer = String.valueOf(add(Double.parseDouble(equationFragments.get(i-1)), Double.parseDouble(equationFragments.get(i+1))));
                        equationFragments.set(i, tempAnswer);
                        equationFragments.remove(i+1);
                        equationFragments.remove(i-1);
                        break;
                    case ("-"): // subtraction
                        tempAnswer = String.valueOf(subtract(Double.parseDouble(equationFragments.get(i-1)), Double.parseDouble(equationFragments.get(i+1))));
                        equationFragments.set(i, tempAnswer);
                        equationFragments.remove(i+1);
                        equationFragments.remove(i-1);
                }
            }

            // CLEANER, because factorial calculation actually creates an empty string fragment and that breaks the whole thing. too lazy to write better code
            for (int i = 0; i < equationFragments.size(); i++){
                if (equationFragments.get(i).equals("")){
                    equationFragments.remove(i);
                }
            }
        }

        // Return answer
        answer = Double.parseDouble(equationFragments.get(0)); // by this point, all other fragments are gone except 1
        return answer;
    }

    private static ArrayList<String> createFragments(String equation){
        // VARIABLES
        ArrayList<String> fragments = new ArrayList<String>();
        char[] equationChars = equation.toCharArray();

        // Fragmentation Loop
        String fragment = "";
        for (int i = 0; i < equation.length(); i++){ // I liked writing the fragment loop better since the logic is much easier to execute into code than the segment jungle
            String charString = Character.toString(equationChars[i]);
            if (operators.contains(charString)){ // if the current character is an operator
                fragments.add(fragment); // this finishes the old fragment, because there's an operator now. THIS IS HOW DECIMAL SUPPORT IS GIVEN!
                fragments.add(charString); // adds the operator. duh
                fragment = "";
            } else {
                fragment += Character.toString(equationChars[i]); // builds next fragment up, character by character
            }
        }
        fragments.add(fragment); // this basically takes whatever's left as its own fragment. This because no other operators were discovered so we can safely assume this is a good number.
        return fragments;
    }

    /* Operations */
    private static double add(double a, double b){
        return (double) a + b;
    }
    
    private static double subtract(double a, double b){
        return (double) a - b;
    }
    
    private static double multiply(double x, double y){
        return (double) x*y;
    }
    
    private static double divide(double x, double y){
        return (double) x/y;
    }
    
    private static double modulo(double x, double y){
        return (double) (x%y);
    }
    
    private static double toPower(double a, double p){
        int aInt = (int) a, pInt = (int) p;
        return (double) (aInt^pInt);
    }

    private static double factorial(double a){
        String e = String.valueOf(a);
        for (int i = ((int) a)-1; i >= 1; i--){ // loops from a-1 to 1
            e += ("*"+String.valueOf((double) i));
        }
        // resulting value should be n*(n-1)*(n-2)*...*1
        // for example, 5! = 5*4*3*2*1

        return calcHandler(e); // this calls calcHandler with the new equation. I initially wrote this without using recursion and Mr. J made fun of me for it so I did remade it with recursion.
    }
}