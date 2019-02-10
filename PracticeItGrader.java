import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.time.format.*;
import java.time.*;

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
 * Version 1.2.1 - 10/11/18 fixed first names with a single space in them e.g "Mary Jo"
 * Version 1.3 - 12/30/18 Split out Problem & Student into their own files & 
 *                      changed static student & problem data structures to locals passed as parameters
 * Version 1.3.1 - 12/30/18 added ifEncrypt to scramble student usernames
 * Version 1.4 - 12/30/18 added dtDeadline to count problems by deadline
 * Version 1.4.1 - 2/10/19 dtDeadline only counts assigned & drops "T<HH:MM:SS>" 
 */
public class PracticeItGrader {
    // Set to true to output diagnostic debugging info
    static boolean ifDebug = false;
    // Set to true to change real student names (George) into hashed letters (AFLTZ)
    static boolean ifEncrypt = false;
    // Set to year,mo,day,h,m,s to calculate # of problems before that time
    static LocalDateTime dtDeadline = null; // LocalDateTime.of(2019,1,20,23,59,59);
    
    public static void main(String[] args) throws FileNotFoundException {
        // Problem class - type, number, time
        ArrayList<Problem> problemList = null;

        // Student class - name, array of problems
        ArrayList<Student> studentList;
        
        // true if a list of student names text file exists to filter the results
        boolean ifClassList;

        studentList = Student.readStudents();
        ifClassList = studentList == null ? false : true;

        // Reads problems performed by students and stores into studentList, will create list if null
        studentList = Problem.readProblems(studentList);

        // Reads list of assigned problems to filter results by
        problemList = Problem.readAssignedProblems();

        printResults(ifClassList, problemList, studentList);
    }

    /**
     * printResults - prints the results from class members
     * 
     * @param ifClassList - if false, just print the list of students
     * @param problemList - list of problems done by student
     * @param studentList - list of students
     *
     * username firstname Lastname #15 Attempted 2 of 12
     *   Missing: SC 10:16 SC 10:18 Ex 10:2 Ex 10:3 Ex 10:4 Ex 10:6 Ex 10:7 Ex 10:10 Ex 10:12 Ex 10:14 Ex 10:15 
     *   Failed: SC 10:8 
     *   Extras: SC 10:2 
     */
    public static void printResults(Boolean ifClassList, ArrayList<Problem> problemList, ArrayList<Student> studentList) {
        // Output class list
        int iStudent = 1;
        for (Student s : studentList) {
            // Always print out the student usernames, if no class list this is all we'll do
            if (ifEncrypt)
                // Print encrypted names to hide student info when sharing samples
                System.out.printf("%s %s %s #%d Attempted %d of %d\n", 
                        Student.toHash(s.getUserName()), Student.toHash(s.getFirstName()), Student.toHash(s.getLastName()), iStudent++, 
                        s.getProblems() != null ? s.getProblems().size() : 0, 
                                problemList != null ? problemList.size() : 0);
            else
                System.out.printf("%s %s %s #%d Attempted %d of %d\n", 
                        s.getUserName(), s.getFirstName(), s.getLastName(), iStudent++, 
                        s.getProblems() != null ? s.getProblems().size() : 0, 
                                problemList != null ? problemList.size() : 0);

            // Only print problems if we already have the class list
            if (ifClassList) {
                ArrayList<Problem> assigned = (ArrayList<Problem>) problemList.clone();
                ArrayList<Problem> extras = new ArrayList<Problem>();
                ArrayList<Problem> failed = new ArrayList<Problem>();

                int countAttemptByDeadline = 0;
                
                // Process each problem for printing
                for (int iProblem = 0; iProblem<s.getProblems().size(); iProblem++) {
                    Problem p = s.getProblems().get(iProblem);

                    int iRemove = -1;
                    if (assigned != null && (iRemove = assigned.indexOf(p)) != -1) {
                        // Problem is on the assigned list
                        // check if it's done by deadline
                        if (dtDeadline != null && p.date.compareTo(dtDeadline) <= 0) {
                            countAttemptByDeadline++;
                        }
                        
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

                // Print "Missing SC 1.2", "Failed: SC 2.3", "Extras: Ex 3.4"
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
                
                // Print how many completed by deadline
                if (dtDeadline != null) {
                    System.out.println("\t" + countAttemptByDeadline + 
                            " assigned attempted before " + 
                            dtDeadline.toString().substring(0,10));
                }
            }

            // newline for each student
            System.out.println();

        }
        System.out.println();
    }
}

/**
 * Comparator to sort problems by time instead of the default chapter:verse
 */
class Sortproblembytime implements Comparator<Problem> 
{ 
    // Used for sorting in ascending order of time
    @Override
    public int compare(Problem a, Problem b) 
    {
        if (a.getDate() == null)
            return -1;
        else if (b.getDate() == null)
            return 1;
        else
            return a.getDate().compareTo(b.getDate());
    } 
} 




