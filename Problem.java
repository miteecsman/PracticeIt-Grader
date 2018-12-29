/**
 * Class Problem
 * 
 * Stores a PracticeIt Problem (e.g. SC 1.1 or Ex 3G.11a) - type ("SC"/"Ex"), chapter, verse, completed
 * 
 * See readProblemNumber for details on how to handle 3G or 11a
 * 
 * @author George
 *
 */
class Problem implements Comparable<Problem> {
    String type;
    int chapter; 
    int number;
    boolean ifCompleted;


    public Problem(String type, int chapter, int number, boolean ifCompleted) {
        super();
        this.type = type;
        this.chapter = chapter;
        this.number = number;
        this.ifCompleted = ifCompleted;
    }

    public Problem(String type, int chapter, int number) {
        super();
        this.type = type;
        this.chapter = chapter;
        this.number = number;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getChapter() {
        return chapter;
    }
    public void setChapter(int chapter) {
        this.chapter = chapter;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }


    public boolean isIfCompleted() {
        return ifCompleted;
    }
    public void setIfCompleted(boolean ifCompleted) {
        this.ifCompleted = ifCompleted;
    }
    @Override
    public String toString() {
        if (type.equals("Self-Check")) 
            return "SC " + chapter + ":" + number + " ";
        else
            return "Ex " + chapter + ":" + number + " ";
    }

    @Override
    public int compareTo(Problem p) {
        // Self-Check before Exercise
        if (!p.type.equals(this.type)) {
            // reverse the normal string order
            return p.type.compareTo(this.type);
            //                              int pType = p.type.equals("Self-check") ? 1 : 2;
            //                              int tType = this.type.equals("Self-check") ? 1 : 2;
            //                              return tType < pType ? -1 : 1;
        }
        // Chapters first
        if (p.chapter != this.chapter) {
            return this.chapter < p.chapter ? -1 : 1;
        }
        // then Numbers
        if (p.number != this.number) {
            return this.number < p.number ? -1 : 1;
        }
        // must be equal
        return 0;

    }
    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Student or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Problem)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members 
        Problem p = (Problem) o;

        Boolean f = p.getType().equalsIgnoreCase(this.getType()) &&
                p.getChapter() == this.getChapter() &&
                p.getNumber() == this.getNumber();

        return (f);

    }

}

