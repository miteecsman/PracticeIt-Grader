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
 * Version 1.5 - 9/30/19 changed from using TXT scraping to CSV file due to PracticeIt changes
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
     * Strips the "" from around a quoted string from a CSV file
     * 
     * @param quoted - "Hello"
     * @return Hello
     */
    public static String stripQuotes(String quoted) {
        if (PracticeItGrader.ifDebug && 
                (quoted.charAt(0) != '"' || quoted.charAt(quoted.length()-1) != '"'))
            System.out.printf("stripQuotes - encountered %s but expected quotes around it", quoted);
        return quoted.substring(1, quoted.length()-1);
    }

    public static enum CSV {USER, LAST, FIRST, PROBLEM, SOLVED, DATETIME, TRIES, CODE} 

    /**
     * Splits the PracticeIt Problem combined descriptor into separate fields
     * 
     * CSV gives us whole "BJP4 Exercise 8.07: addTimeSpan" which needs to be split out
     *                       0       1      2        3
     * 
     * @param results - array of CSV fields, 4th item contains the problem description
     * @param chapterVerse - modified here to hold the chapter & problem number
     * @return String with Exercise/Self-Check
     */
    public static String splitPIProblem(String results[], int[] chapterVerse) {
        // Field PROBLEM needs to be split up
        // CSV gives us whole "BJP4 Exercise 8.07: addTimeSpan" which needs to be split out
        //                      0       1      2        3
        String piTypeCombined = results[CSV.PROBLEM.ordinal()];
        String[] piTypes = piTypeCombined.split("[ ]+");

        // Validate problem format
        if (piTypes.length != 4 ||
                !piTypes[0].substring(0, 3).equals("BJP") ||
                (!(piTypes[1].equals("Exercise") || piTypes[1].equals("Self-Check"))))
            System.out.printf("readProblems unexpected problem format: %s\n", piTypeCombined);

        // Problem Subfield 2: Split 1:10 into 1 and 10
        int[] chapterVerseTemp = Problem.splitProblemNumber(piTypes[2]);
        System.arraycopy(chapterVerseTemp, 0, chapterVerse, 0, chapterVerseTemp.length);

        // Problem Subfield 0: We ignore BJPX field
        // Problem Subfield 3: We ignore problem name for now
        // Problem Subfield 1: Exercise or Self-Check
        return piTypes[1];
    }
    
    /**
     * readProblems - reads all problems from PracticeIt student results, stores into class arrays
     * 
     * Format of header line
     *        "Username","Last","First","Problem","Solved?","Date/Time","Tries","Solution Code"
     *             0        1      2        3         4         5          6         7
     *        "abcmoney6","Doe","John","BJP4 Exercise 8.07: addTimeSpan","No","2019-09-25 16:45:33","1","//test code"
     * After header is usually code lines which are skipped
     */
    public static ArrayList<Student> readProblems(ArrayList<Student> studentList) throws FileNotFoundException {
        boolean ifSkip = false;
        int problemNum = 1;
        String line; // raw line
        String results[]; // split line
        String ignoredStudent = "";   // debug only
        PrintStream ps = null;

        if (PracticeItGrader.ifDebug) {
            System.out.println();
            System.out.println("readProblems Begin");
        }

        // If no list of class members, must initialize this 
        Boolean ifClassList = studentList == null ? false : true;
        if (studentList == null)
            studentList = new ArrayList<Student>();

        File f = new File("practice-it.csv");
        if (!f.canRead()) {
            System.out.println("Can't find file");
        }
        Scanner sc = new Scanner(f);

        // If we're encrypting the student usernames, write the file out
        if (PracticeItGrader.ifEncrypt) {
            File fEncrypt = new File("Encrypted Results.txt");
            ps = new PrintStream(fEncrypt);
        }

        // get headers & verify
        line = sc.nextLine(); 
        results = line.split("[,]+");
        if (results.length != 8 || 
                !stripQuotes(results[CSV.USER.ordinal()]).equals("Username") ||
                !stripQuotes(results[CSV.LAST.ordinal()]).equals("Last") ||
                !stripQuotes(results[CSV.CODE.ordinal()]).equals("Solution Code"))
            System.out.printf("readProblems expected first line to be headers but found %s",line);

        // Loop through all lines
        while (sc.hasNextLine()) {
            ///////////////////////////////////////////////////////
            // Read lines, discard code lines 
            ///////////////////////////////////////////////////////

            line = sc.nextLine();
            // debugging help - put the item item # here and set breakpoint 
//            if (problemNum == 246)
//             System.out.println(line);               

            // If we're in code, need to skip lines.
            if (ifSkip) {
                // Line could be blank
                if (line.length() != 0) {
                    //   Code ends with a line ending in a single quote
                    if (line.charAt(line.length()-1) == '"' &&
                            line.charAt(line.length()-2) != '"') {
                        ifSkip = false;
                    }
                }

                // If encrypting file, still need to output code lines
                if (PracticeItGrader.ifEncrypt && ifSkip)
                    ps.println(line);

                continue;
            }
            
            // Print out each problem header
            if (PracticeItGrader.ifDebug)
                System.out.printf("%d, %s\n", problemNum, line);
            
            // Code starts by ending a line without a single quote
            if (line.charAt(line.length()-1) != '"' 
                    //   except for the weird case where it just has a single quote to open the code on next line
                    //   which we can identify by a comma right before the quote
                    || line.charAt(line.length()-2) == ',' )
                ifSkip = true;
            problemNum++;

            ///////////////////////////////////////////////////////
            // Split header into fields needed to construct Student & Problem 
            ///////////////////////////////////////////////////////

            // Format of line
            // 
            // This is the new CSV format that we are given
            // "Username","Last","First","Problem","Solved?","Date/Time","Tries","Solution Code"
            // New  0        1      2        3         4         5          6         7
            // "abcmoney6","Doe","John","BJP4 Exercise 8.07: addTimeSpan","No","2019-09-25 16:45:33","1","//test code"

            // Split header into fields and strip quotes
            results = line.split("[,]+");
            for (int index = CSV.USER.ordinal(); index < CSV.CODE.ordinal(); index++)
                results[index] = stripQuotes(results[index]);

            // Field USER, LAST, FIRST used directly
            String userName = results[CSV.USER.ordinal()];
            String lastName = results[CSV.LAST.ordinal()];
            String firstName = results[CSV.FIRST.ordinal()];

            // if encrypting, output the line but replace student names with encrypted
            if (PracticeItGrader.ifEncrypt) {
                line = line.replace(userName, Student.toHash(userName));
                line = line.replace(lastName, Student.toHash(lastName));
                line = line.replace(firstName, Student.toHash(firstName));
                ps.println(line);
            }

            int[] chapterVerse = new int[2];
            String type = splitPIProblem(results, chapterVerse);

            // Field SOLVED is either Y or N
            Boolean comp = results[CSV.SOLVED.ordinal()].toUpperCase().charAt(0) == 'Y';

            // Field DATETIME, TRIES, CODE unused at this time
            // Eventually we should examine DATETIME to find average time taken & unusual times
            
            ///////////////////////////////////////////////////////
            // Done reading the file, now creating data structures 
            ///////////////////////////////////////////////////////
            
            // heavy debugging - print out parsed fields
            // System.out.printf("user=%s, first=%s, last=%s, type=%s, chapter=%d, verse=%d, solved=%b\n", userName, firstName, lastName, type, chapterVerse[0], chapterVerse[1], comp);

            // build or lookup the student
            Student s = new Student(userName, firstName, lastName);
            int studentNum = studentList.indexOf(s);
            if (!ifClassList) {
                if (studentNum == -1)
                    // Build Class List if not supplied
                    studentList.add(s);
            } else {
                // Print student's name once, skip repeats
                if (PracticeItGrader.ifDebug && !s.getUserName().equalsIgnoreCase(ignoredStudent)) {
                    if (ignoredStudent.length() != 0)
                        // No need for line break after problems if this is the first student 
                        System.out.println();
                    System.out.println((studentNum == -1 ? "Skipping " : "Starting ") + s);
                    // We don't want to see this student name anymore
                    ignoredStudent = s.getUserName(); 
                    // reset problem type for correct linebreaks
                    // currentPIType = null; see below for old usage 
                }
                if (studentNum != -1)
                    // Find existing student record
                    s = studentList.get(studentNum);
            }
            
            // Get time problem was submitted
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ( "yyyy-MM-dd HH:mm:ss" , Locale.ENGLISH );
            LocalDateTime date = LocalDateTime.parse(results[CSV.DATETIME.ordinal()], formatter);

            // Add problem to the student
            if (studentNum != -1) {
                Problem p = new Problem(type, chapterVerse[0], chapterVerse[1], comp, date);
                s.getProblems().add(p);
            }

        }
        if (PracticeItGrader.ifDebug) {
            System.out.println();
            System.out.println("readProblems End");
        }

        return studentList;     // in case it was null to being with
    }
}
    



