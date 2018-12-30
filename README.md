# PracticeIt-Grader
Grades PracticeIt student work

This is for people with "instructor" accounts on PracticeIt.  Unfortunately they no longer give
out accounts for college level classes, but they will for high schools.  I don't have anything
to do with running PracticeIt so I can't comment on why this is.

The input is a scraping of the instructor's results view.  This is intended to be run in 3 passes 
to build up the list of students, assigned problems, and finally the complete results.

```
  Usage: 
       Called with just "PracticeIt Results Raw.txt" (scrape website results manually)
               Writes list of students
               Remove any unwanted students and supply next time to filter list
   Called with raw results & "Student Usernames.txt"
       Writes filtered list of students and their problems
       Modify desired problems and save as "Assigned problems.txt"
   Called with raw results, student usernames, "Assigned problems.txt"
       Looks for header "SC" and all following problems will be assumed SC
         any other header is assumed to be "Ex"
       Writes filtered list of students with how they did on assigned problems
   Note Chapter 3G translated into 20, letter problems (11a) add 100
```

[Youtube Video on How to use](https://youtu.be/lOSCqyhS1bc)
