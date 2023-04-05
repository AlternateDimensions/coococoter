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
        "TEST CASES: 5+5 = 10 | 4*(3+3) = 24 | 6!+5^4 = 1345 | (5+5*3)/(7^2)+3! ~= 6.41 | (5*[3+4^4]+2)+(3!) = 1303 | -(4*3) = -12 | 4(2+2) = 16 | 2(3+1)3 = 24 |",
        "FUNCTIONS: + | - | * | / | Factorial ! | Power ^ | Modulo % | PFEMMDAS Order (L -> R) | Parenthesis/Bracket () [] {} | Distributive Multiplcation x(y) |",
        "NOTE: Factorials of decimals (gamma functions) are not supported!",
        "TIP: To clear output history, enter \"clear\"",
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
                e.printStackTrace();
                try{Thread.sleep(50000);}catch(Exception aa){}
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
            System.out.print("\n");
            System.out.print("\033[2K");
            System.out.print(Colors.CLEAR+Colors.BOLD + "Input equation: " + Colors.CYAN + Colors.UNDERLINE);
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
        for (Character c : separatorOpenings.toCharArray()){ // For each character in separators string
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
        int equationOLength = equation.length(), diff = 0;

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
                    unclosedSegment++; // there is another segment being made. will process later
                }
            } else if (sepType != Separators.NONE && sepType == sepTypeNeeded){ // this is the closing we are looking for
                if (unclosedSegment == 1){ // Original segment to close
                    endIndex = i;
                    segments.add(new Segment(equation.substring(startIndex+1, endIndex), startIndex, endIndex)); // add new segment
                    sepTypeNeeded = Separators.NONE;
                    startIndex = 0; endIndex = 0;
                }
                unclosedSegment--; // segment closes, regardless if it's the one we want.
            }
        }

        if (segments.size() == 0){ // this is here in case a segment isn't made, typically because there was no closing brackets
            segments.add(new Segment(equation.substring(startIndex+1, equation.length()), startIndex, equation.length()));
        }

        // iterate each segment to get answers and simplify
        for (Segment s : segments){
            // get answer of segment
            answer = calcHandler(s.segment);

            // replace entire segment substring with answer
            if (s.indexStart-diff == 0 && s.indexEnd-diff == equation.length()-1){
                equation = String.valueOf(answer);
            } else if (s.indexStart == 0-diff){
                if (operators.contains(equation.substring(s.indexEnd+1-diff, s.indexEnd+2-diff))){
                    equation = answer + equation.substring(s.indexEnd+1-diff); // has a connecting operator
                } else {
                    equation = answer + "*" + equation.substring(s.indexEnd+1-diff); // does not have a connecting operator
                }
            } else if (s.indexEnd-diff == equation.length()-1){
                if (operators.contains(equation.substring(s.indexStart-diff-1, s.indexStart-diff))){
                    equation = equation.substring(0, s.indexStart-diff) + answer; // has a connecting operator
                } else {
                    equation = equation.substring(0, s.indexStart-diff) + "*" + answer; // does not have a connecting operator
                }
            } else {
                if (operators.contains(equation.substring(s.indexEnd+1-diff, s.indexEnd+2-diff))){
                    equation = answer + equation.substring(s.indexEnd+1-diff); // has a connecting operator
                } else if (operators.contains(equation.substring(s.indexStart-diff-1, s.indexStart-diff))){
                    equation = equation.substring(0, s.indexStart-diff) + answer; // has a connecting operator
                } else {
                    equation = equation.substring(0, s.indexStart-diff) + "*" + answer + "*" + equation.substring(s.indexEnd+1-diff);
                }
            }

            // adjust values since the size of the equation changes with substitution.
            diff = equationOLength - equation.length();
            equationOLength = equation.length();
        }      
        // Supposedly once the segments run out, they should just return the normal answer.
        answer = calcHandler(equation);
        return answer;
    }

    /* evaluation actually evaluates equations, when broken down, and returns the resulting value */
    private static double evaluate(String equation){
        // Have to do this check here in order to cleanly sub it back.
        equation = equation.replaceAll("\\)", "").replaceAll("\\]", "").replaceAll("\\}", "");
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

            // Operation check
            if (charString.equals("-") && fragment.equals("")){
                // probably a negative sign, evaluate as such
                fragment += charString;
            } else if (operators.contains(charString)){ // if the current character is an operator
                if (!fragment.equals("")){
                    fragments.add(fragment); // this finishes the old fragment (if any), because there's an operator now. THIS IS HOW DECIMAL SUPPORT IS GIVEN!
                }
                fragments.add(charString); // adds the operator. duh
                fragment = "";
            } else {
                fragment += charString; // builds next fragment up, character by character
            }
        }
        if (!fragment.equals("")){
            fragments.add(fragment); // this basically takes whatever's left as its own fragment. This because no other operators were discovered so we can safely assume this is a good number.
        }
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
        return (double) (Math.pow(a, p));
    }

    private static double factorial(double a){
       if (a%1 == 0){
        if (a == 0.0){return 1.0;}
        return (a*factorial(a-1.0));
       }
       return 0;
       // return Gamma.gamma(a);
    }
}