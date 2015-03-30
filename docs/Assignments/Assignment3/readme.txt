References
	- http://cglab.ca/~cdillaba/comp5008/mulmuley.html
	- http://www.cs.umd.edu/class/fall2014/cmsc754/Lects/lect09.pdf
	- class lecture notes
	
To Build: 
	- install apache ant and Java8 and make sure both are in path for host system (can be manually set in build/run scripts)
	- use build.bat (windows) or build.sh (linux)
To Run: 
	cd runScripts/Assignment3
	run_assignment3.(bat/sh) <input_file>
	
NOTES:

I had a tough time getting the node merging to work correctly. The assignment builds and runs but 
crashes with attempting to insert the third line segment. I believe it has something to do with
when I go to insert either nodes that need to be merged or the merged nodes after-the-fact. Either
I'm creating the map hierarchy wrong or I'm updating the trapezoid neighbors incorrectly. 

I've created a file, diagram.png, with the trapezoid graph that the system outputs before crashing and 
there do seem to be issues. If I end it after inserting the first segment, the map is correct. It also 
looks correct after the second segment (if I remember correctly) but after the third, it requires some 
trapezoids to be merged and it has trouble. However, the map.png is what the map should have looked 
like had it worked correctly. The matrix.pdf is the output of the application before crashing with the
row/column sums added and color-coded. If you run the application, the main program directory will 
contain an "assignment3.csv" with the actual output file.