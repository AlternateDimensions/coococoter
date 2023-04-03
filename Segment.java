public class Segment {
    public String segment;
    public int indexStart, indexEnd;

    public Segment (String s, int is, int ie){
        segment = s;
        indexStart = is;
        indexEnd = ie;
    }
}

// Needed something to store Segment information. and im not writing that janky ahh ArrayList<Object> crap that I could do without having to make a whole new class. Oh well.
