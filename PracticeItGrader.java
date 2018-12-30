import java.util.*;
import java.io.*;
import java.security.MessageDigest;

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
    // Set to true to output diagnostic debugging info
    static boolean ifDebug = false;

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
     *
     * andynou Andy Nou #15 Attempted 2 of 12
     *   Missing: SC 10:16 SC 10:18 Ex 10:2 Ex 10:3 Ex 10:4 Ex 10:6 Ex 10:7 Ex 10:10 Ex 10:12 Ex 10:14 Ex 10:15 
     *   Failed: SC 10:8 
     *   Extras: SC 10:2 
     */
    public static void printResults(Boolean ifClassList, ArrayList<Problem> problemList, ArrayList<Student> studentList) {
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




