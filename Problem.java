import java.io.*;
import java.util.*;
import java.time.format.*;
import java.time.*;

/**
 * Class Problem
 * 
 * Stores a PracticeIt Problem (e.g. SC 1.1 or Ex 3G.11a) - type ("SC"/"Ex"), chapter, verse, completed
 * 
 * See readProblemNumber for details on how to handle 3G or 11a
 * 
 * @author George Hu
 * 
 * Version 1.3 - 12/30/18 Split out Problem & Student into their own files & 
 *                      changed static student & problem data structures to locals passed as parameters
 * Version 1.3.1 - 12/30/18 added ifEncrypt to scramble student usernames
 * Version 1.4 - 12/30/18 added date field to count problems by deadline
 *
 */
class Problem implements Comparable<Problem> {
    String type;
    int chapter; 
    int number;
    boolean ifCompleted;
    LocalDateTime date;

    public Problem(String type, int chapter, int number, boolean ifCompleted, LocalDateTime date) {
        super();
        this.type = type;
        this.chapter = chapter;
        this.number = number;
        this.ifCompleted = ifCompleted;
        this.date = date;
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

    public LocalDateTime getDate() {
        return this.date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
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

    /**
     * splitProblemNumber
     * 
     * Splits "3:10" into array of chapter, verse
     * 
     * @param token
     * @return
     *      int array[0] = chapter.  If chapter = 3G, chapter reassigned to unused chapter 20
     *      int array[1] = verse.  If problem = 11a (or any 2 digit + a/b/c...), problem reassigned 100 + 11 + 1=a,2=b
     */
    public static int[] splitProblemNumber(String token) {
        int[] chapterVerse = new int[2];
        String[] problem = token.split("[ .:]+");
        if (PracticeItGrader.ifDebug)
            System.out.println(problem[0] + ":" + problem[1]);
        if (problem[0].length() == 2 && problem[0].charAt(1) == 'G') {
            // this must be Chapter 3G
            // Currently chapters can only be integers, so assign it chapter 20
            chapterVerse[0] = 20;
        } else
            chapterVerse[0] = Integer.parseInt(problem[0]);
        if (problem[1].length() == 3 && !Character.isDigit(problem[1].charAt(2))) {
            // this must be a problem 11a, etc
            // Currently problems can only be integers, so assign it problem 100+11+a=1,b=2,etc
            chapterVerse[1] = 100 + Integer.parseInt(problem[1].substring(0, 2)) + problem[1].charAt(2) - 'a';
        } else
            chapterVerse[1] = Integer.parseInt(problem[1]);
        return chapterVerse;
    }

    /**
     * readAssignedProblems - reads problem list from file
     * 
     * Assumes file starts with "type" which is either "SC" for self-check or anything else is treated as an Exercise
     * 
     * This can handle either a list of multiple problems after a SC/EX type
     *  SC 1.1 1.5 1.7 Ex 1.1 1.2 1.10
     * or a type before each problem
     *  SC 1.1 SC 1.5 SC 1.7 Ex 1.1 Ex 1.2 Ex 1.10
     * 
     * @return ArrayList of Problems assigned
     * @throws FileNotFoundException
     */
    public static ArrayList<Problem> readAssignedProblems() throws FileNotFoundException {
        // If "Assigned Problems.txt" exists, load it, use it at end in printing
        // Exercise  9:11  9:4  9:9 Self-Check  9:10  9:3  9:8  9:9 
        // SC 1:2 3:4 Ex 5:6 7:8
        ArrayList<Problem> problemList = new ArrayList<Problem>();
        File fProblems = new File("Assigned Problems.txt");
        if (fProblems.canRead()) {
            Scanner scProblems = new Scanner (fProblems);
            String token = scProblems.next();
            String problemType = token;

            // loop with "current" token which could be problem type or chapter:verse
            do {
                // if not number, must be problem type
                if (!Character.isDigit(token.charAt(0))) {
                    if (token.equals("SC"))
                        problemType = "Self-Check";
                    else
                        problemType = "Exercise";
                }
                else 
                {
                    int[] chapterVerse = Problem.splitProblemNumber(token);
                    problemList.add(new Problem(problemType, chapterVerse[0], chapterVerse[1]));
                }
                // this is for the next outer loop
                token = scProblems.hasNext() ? scProblems.next() : null;
            } while (token != null);
            // go back to next problem type or end
        }
        return problemList;
    }

    /**
     * readProblems - reads all problems from PracticeIt student results, stores into class arrays
     * 
     * Format of line
     * <problem#> <username> <last> <first> <book PIType number name> <completed> <2018-04-04 22:18:01> <tries>
     *     0           1       2      3       4    5      6     7         8            9         10       11
     *  OR it could be code
     */
    public static ArrayList<Student> readProblems(ArrayList<Student> studentList) throws FileNotFoundException {
        boolean ifSkip = false;
        int problemNum = 0;
        String line; // raw line
        String results[]; // split line
        String ignoredStudent = "";
        PrintStream ps = null;

        // If no list of class members, must initialize this 
        Boolean ifClassList = studentList == null ? false : true;
        if (studentList == null)
            studentList = new ArrayList<Student>();
        
        // Read PracticeIt results into structures
        File f = new File("PracticeIt Results Raw.txt");
        if (!f.canRead()) {
            System.out.println("Can't find file");
        }
        Scanner sc = new Scanner(f);

        // If we're encrypting the student usernames, write the file out
        if (PracticeItGrader.ifEncrypt) {
            File fEncrypt = new File("Encrypted Results.txt");
            ps = new PrintStream(fEncrypt);
        }
        
        while (sc.hasNextLine()) {

            // Get a line, or skip lines until a valid line is found
            do {
                // debugging help - put the item item # here and set breakpoint 
                // if (problemNum == 111)
                //    System.out.println("we're here!!!");
                
                // We could be skipping code lines when the file ends
                if (!sc.hasNextLine())
                    return studentList;

                // Format of line
                // <problem#> <username> <last> <first> <book PIType number name> <completed> <2018-04-04 22:18:01> <tries>
                //      0           1       2      3       4    5      6     7         8            9         10       11
                // OR it could be code

                line = sc.nextLine();
                // System.out.println(line);               // debugging code to print every line
                results = line.split("[\t ]+");
                // Skip code lines or blank lines
                // if line is just \t it returns a zero length array
                if (results.length != 0) {
                    // check if this is code 
                    Scanner scTemp = new Scanner(results[0]);
                    // it needs to start with digit in col 0 & be the next problem number
                    if (results[0].length() != 0 && Character.isDigit(results[0].charAt(0)) && scTemp.hasNextInt() && scTemp.nextInt() == problemNum + 1)
                        // next problem
                        ifSkip = false;
                    else
                        // in code
                        ifSkip = true;
                } else
                    ifSkip = true;
                
                // If encrypting file, still need to output code lines
                if (PracticeItGrader.ifEncrypt && ifSkip)
                    ps.println(line);
            } while (ifSkip);

            // Split line into tokens
            // <problem#> <username> <last> <first> <book PIType number name> <completed> <2018-04-04 22:18:01> <tries>
            //      0           1       2      3       4    5      6     7         8            9         10       11
            problemNum = Integer.parseInt(results[0]);

            String username = results[1];
            String lastName = results[2];
            String firstName = results[3];
            
            // if encrypting, output the line but replace student names with encrypted
            if (PracticeItGrader.ifEncrypt) {
                line = line.replace(username, Student.toHash(username));
                line = line.replace(lastName, Student.toHash(lastName));
                line = line.replace(firstName, Student.toHash(firstName));
                ps.println(line);
            }
            
            // Some firstnames have a space in it such as "Mary Jo"
            // This causes it to parse the name into two spots, so we will ignore the
            // second word and check that we find "BJP" in the next spot
            if (results[4].indexOf("BJP") != 0) {
                if (results[5].indexOf("BJP") != 0)
                    System.out.println("ERROR - can't parse entry " + problemNum);
                
                // We need to slide results 5.. back to 4..
                for (int x = 5; x <= 11; x++)
                    results[x-1] = results[x];
            }
            
            String type = results[5];
            //                        String problemName = results[7];
            Boolean comp = (results[8].toUpperCase().charAt(0) == 'Y');

            // Split 1:10 into 1 and 10
            int[] chapterVerse = Problem.splitProblemNumber(results[6]);

            // build or lookup the student
            Student s = new Student(username, firstName, lastName);
            int studentNum = studentList.indexOf(s);
            if (!ifClassList) {
                if (studentNum == -1)
                    // Build Class List if not supplied
                    studentList.add(s);
            } else {
                // This may be a student who is no longer in the class list
                if (studentNum == -1) {
                    if (!s.getUserName().equalsIgnoreCase(ignoredStudent)) {
                        // Only print this once per ignored student
                        if (PracticeItGrader.ifDebug) System.out.println("Skipping " + s);
                        ignoredStudent = s.getUserName();
                    }
                } else
                    s = studentList.get(studentNum);
            }

            // Get time problem was submitted
            String dateTime = results[9] + " " + results[10];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ( "yyyy-MM-dd HH:mm:ss" , Locale.ENGLISH );
            LocalDateTime date = LocalDateTime.parse(dateTime, formatter);
            
            // Add problem to the student
            if (studentNum != -1) {
                Problem p = new Problem(type, chapterVerse[0], chapterVerse[1], comp, date);
                s.getProblems().add(p);
            }

        }
        return studentList;     // in case it was null to being with
    }


}

