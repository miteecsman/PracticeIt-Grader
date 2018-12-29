import java.util.*;
import java.io.*;

/**
 * 
 * PracticeItGrader
 * 
 * Usage: 
 *      Called with just "PracticeIt Results Raw.txt" (scrape website results manually)
 *              Writes list of students
 *              Remove any unwanted students and supply next time to filter list
 * 
 *  Called with raw results & "Student Usernames.txt"
 *      Writes filtered list of students and their problems
 *      Modify desired problems and save as "Assigned problems.txt"
 *  
 *  Called with raw results, student usernames, "Assigned problems.txt"
 *      Looks for header "SC" and all following problems will be assumed SC
 *        any other header is assumed to be "Ex"
 *      Writes filtered list of students with how they did on assigned problems
 * 
 *  Note Chapter 3G translated into 20, letter problems (11a) add 100
 *  
 * @author George Hu
 *
 * Version 1.0 - 4/14/18 initial version
 * Version 1.1 - 4/26/18 fixed chapter 3G -> 20, 11a/b/c -> 111/2/3
 * Version 1.2 - 5/29/18 added totals to Missing/Failed/Extras
 * Version 1.2.1 - 10/11/18 fixed first names with a single space in them "Jae Hyeon"
 */
public class PracticeItGrader {
    static ArrayList<Student> studentList = null;
    static ArrayList<Problem> problemList = null;
    static Scanner sc;
    static boolean ifClassList;
    static boolean ifDebug = false;

    public static void main(String[] args) throws FileNotFoundException {
        // Problem class - type, number, time
        // Student class - name, array of problems

        studentList = new ArrayList<Student>();


        readStudents();

        readAssignedProblems();

        readProblems();

        printResults();
    }

    /**
     * readAssignedProblems - reads problem list from file into class member
     * 
     * This can handle either a string of problems after type
     *  SC 1.1 1.5 1.7 Ex 1.1 1.2 1.10
     * or a type before each problem
     *  SC 1.1 SC 1.5 SC 1.7 Ex 1.1 Ex 1.2 Ex 1.10
     * 
     * @throws FileNotFoundException
     */
    public static void readAssignedProblems() throws FileNotFoundException {
        // If "Assigned Problems.txt" exists, load it, use it at end in printing
        // Exercise  9:11  9:4  9:9 Self-Check  9:10  9:3  9:8  9:9 
        // SC 1:2 3:4 Ex 5:6 7:8
        problemList = new ArrayList<Problem>();
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
                    int[] chapterVerse = splitProblemNumber(token);
                    problemList.add(new Problem(problemType, chapterVerse[0], chapterVerse[1]));
                }
                // this is for the next outer loop
                token = scProblems.hasNext() ? scProblems.next() : null;
            } while (token != null);
            // go back to next problem type or end
        }
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
        if (ifDebug)
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
     * readProblems - reads all problems from PracticeIt student results, stores into class arrays
     * 
     * Format of line
     * <problem#> <username> <last> <first> <book PIType number name> <completed> <2018-04-04 22:18:01> <tries>
     *     0           1       2      3       4    5      6     7         8            9         10       11
     *  OR it could be code
     */
    public static void readProblems() throws FileNotFoundException {
        boolean ifSkip = false;
        int problemNum = 0;
        String results[];
        String ignoredStudent = "";

        // Read PracticeIt results into structures
        File f = new File("PracticeIt Results Raw.txt");
        if (!f.canRead()) {
            System.out.println("Can't find file");
        }
        sc = new Scanner(f);

        while (sc.hasNextLine()) {

            // Get a line, or skip lines until a valid line is found
            do {
                // debugging help - put the item item # here and set breakpoint 
                // if (problemNum == 111)
                //    System.out.println("we're here!!!");
                
                // We could be skipping code lines when the file ends
                if (!sc.hasNextLine())
                    return;

                // Format of line
                // <problem#> <username> <last> <first> <book PIType number name> <completed> <2018-04-04 22:18:01> <tries>
                //      0           1       2      3       4    5      6     7         8            9         10       11
                // OR it could be code

                String line = sc.nextLine();
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
            } while (ifSkip);

            // Split line into tokens
            // <problem#> <username> <last> <first> <book PIType number name> <completed> <2018-04-04 22:18:01> <tries>
            //      0           1       2      3       4    5      6     7         8            9         10       11
            problemNum = Integer.parseInt(results[0]);

            String username = results[1];
            String lastName = results[2];
            String firstName = results[3];
            
            // Some firstnames have a space in it such as "Jae Hyeon"
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
            int[] chapterVerse = splitProblemNumber(results[6]);

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
                        if (ifDebug) System.out.println("Skipping " + s);
                        ignoredStudent = s.getUserName();
                    }
                } else
                    s = studentList.get(studentNum);
            }

            // Add problem to the student
            if (studentNum != -1) {
                Problem p = new Problem(type, chapterVerse[0], chapterVerse[1], comp);
                s.getProblems().add(p);
            }

        }
    }

    /**
     * readStudents - read list of students into class member, stores into class variables
     * 
     * @throws FileNotFoundException
     */
    public static void readStudents() throws FileNotFoundException {
        // If "Student Usernames.txt" file exists, load it
        File fStudents = new File("Student Usernames.txt");
        ifClassList = fStudents.canRead();
        if (ifClassList) {
            Scanner sc = new Scanner(fStudents);
            int i = 1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.length() != 0) {
                    String results[] = line.split("[ ]+");
                    Student s = new Student(results[0], results[1], results[2]);
                    studentList.add(s);
                    if (ifDebug) System.out.println("Found Student " + i++ + ":" + s);
                }
            }
        }

    }


    /**
     * printResults - prints the results from class members
     *
     * andynou Andy Nou #15 Attempted 2 of 12
     *   Missing: SC 10:16 SC 10:18 Ex 10:2 Ex 10:3 Ex 10:4 Ex 10:6 Ex 10:7 Ex 10:10 Ex 10:12 Ex 10:14 Ex 10:15 
     *   Failed: SC 10:8 
     *   Extras: SC 10:2 
     */
    public static void printResults() {
        // Output class list
        int iStudent = 1;
        for (Student s : studentList) {
            System.out.printf("%s %s %s #%d Attempted %d of %d\n", 
                    s.getUserName(), s.getFirstName(), s.getLastName(), iStudent++, 
                    s.getProblems() != null ? s.getProblems().size() : 0, 
                            problemList != null ? problemList.size() : 0);

            // Only print problems if we already have the class list
            //   otherwise we should just print the students names for use in filtering
            if (ifClassList) {
                ArrayList<Problem> assigned = (ArrayList<Problem>) problemList.clone();
                ArrayList<Problem> extras = new ArrayList<Problem>();
                ArrayList<Problem> failed = new ArrayList<Problem>();


                // type starts empty, detect changes
                //                                String type = "";

                // Process each problem for printing
                for (int iProblem = 0; iProblem<s.getProblems().size(); iProblem++) {
                    Problem p = s.getProblems().get(iProblem);

                    int iRemove = -1;
                    if (assigned != null && (iRemove = assigned.indexOf(p)) != -1) {
                        if (!p.isIfCompleted())
                            failed.add(p);
                        // remove it from list of assigned problems
                        assigned.remove(iRemove);
                    } else {
                        // add it to the extras list
                        extras.add(p);
                    }

                }

                Collections.sort(assigned);
                Collections.sort(failed);
                Collections.sort(extras);

                // Print "Missing SC 1.2", "Extras: Ex 3.4"
                System.out.printf("\tMissing %d:", assigned.size());
                for (Problem p: assigned)
                    System.out.printf(p.toString());
                System.out.println();
                System.out.printf("\tFailed %d: ", failed.size());
                for (Problem p: failed)
                    System.out.printf(p.toString());
                System.out.println();
                System.out.printf("\tExtras %d: ", extras.size());
                for (Problem p: extras)
                    System.out.printf(p.toString());
                System.out.println();
            }

            // newline for each student
            System.out.println();

        }
        System.out.println();
    }
}


/**
 * Class Student
 * 
 * This stores information for a PracticeIt student - username, first name, last name, list of problems
 * 
 * @author George
 */
class Student {
    String userName;
    String firstName;
    String lastName;
    ArrayList<Problem> problems;

    public Student(String userName, String firstName, String lastName) {
        super();
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.problems = new ArrayList<Problem>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }

    public void setProblems(ArrayList<Problem> problems) {
        this.problems = problems;
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Student or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Student)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members 
        Student s = (Student) o;

        Boolean f = s.getFirstName().equalsIgnoreCase(this.getFirstName());
        f &= s.getLastName().equalsIgnoreCase(this.getLastName());
        f &= s.getUserName().equalsIgnoreCase(this.getUserName());

        return (f);

    }

    @Override
    public String toString() {
        return "Student [userName=" + userName + ", firstName=" + firstName + ", lastName=" + lastName + ", problems="
                + problems + "]";
    }

}


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

