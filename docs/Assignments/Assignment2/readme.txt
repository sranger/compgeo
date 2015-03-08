Assignment 2
	http://www.lems.brown.edu/~wq/projects/cs252.html
    http://stackoverflow.com/questions/16314069/calculation-of-intersections-between-line-segments

To Build: 
	- install apache ant and Java8 and make sure both are in path for host system (can be manually set in build/run scripts)
	- use build.bat (windows) or build.sh (linux)
To Run: 
	cd runScripts/Assignment2
	run_assignment2_bruteforce.(bat/sh) <number_of_points_or_pointfile> [--ui]
	run_assignment2_linesweep.(bat/sh) <number_of_points_or_pointfile> [--ui]

	- if --ui is given at end of the commands, a window will display a window showing the input line segments and the 
	  output intersection points.
	
	
   run_assignment2_benchmark.(bat/sh) [repeat_count] [--ui]
   
   - will run each algorithm 10 times for each of the following point input counts
      [10,100,1000,10000]
   - the default run count per input size is 10
   - if --ui is given (optional), a window will display with a set of progress bars; the top one showing progress through 
     the segment count tests and the bottom one showing the number of tests run for that input size.
   - an algorithm will be skipped entirely if any of the attempts runs past five minutes
   - no output files will be created as with the individual tests; this is for performance only
   
NOTES:
   - The LineSweep algorithm assumes a few things that, when creating random sample data, would not be practical to verify 
     such as no three segments intersect at the same location. The output for such instances are untested and ignored.