import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Class Student
 * 
 * This stores information for a PracticeIt student - username, first name, last name, list of problems
 * 
 * @author George Hu
 * 
 * Version 1.3 - 12/30/18 Split out Problem & Student into their own files & 
 *                      changed static student & problem data structures to locals passed as parameters
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

    /**
     * readStudents - read list of students into class member, stores into class variables
     * 
     * @return NULL if can't find the list of students
     *          ArrayList of Students found
     * @throws FileNotFoundException
     */
    public static ArrayList<Student> readStudents() throws FileNotFoundException {
        ArrayList<Student> studentList = null;
        
        // If "Student Usernames.txt" file exists, load it
        File fStudents = new File("Student Usernames.txt");
        Boolean ifClassList = fStudents.canRead();
        if (ifClassList) {
            studentList = new ArrayList<Student>();
            Scanner sc = new Scanner(fStudents);
            int i = 1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.length() != 0) {
                    String results[] = line.split("[ ]+");
                    Student s = new Student(results[0], results[1], results[2]);
                    studentList.add(s);
                    if (PracticeItGrader.ifDebug) System.out.println("Found Student " + i++ + ":" + s);
                }
            }
        }
        return studentList;

    }
    
    /**
     * Convert a string to an alphabetic hash for encryption purposes
     * Algorithm: 
     *  take the string's hash code (-12345678)
     *  convert to positive number (12345678)
     *  treat each pair of digits as a char (e.g. 0x12 = dec 18)
     *  shift the char into upperchase chars by taking mod of 26 starting at 'A' (dec 18 -> A + 18 chars)
     *  
     * @param s
     */
    public static String toHash(String s) {
        String input = Integer.toString(Math.abs(s.hashCode()));
        
        // StringBuilder is a more efficient version of String because it's mutable
        StringBuilder c = new StringBuilder();
        int len = input.length();
        for (int i=0;i<len;i+=2) {
            String hexValueString = input.substring(i, Math.min(len, i+2));
            int value1 = Integer.parseInt(hexValueString, 16)%26+65;
            c.append(Character.toString((char) value1));
        }
        return (c.toString());
    }
    
}
