References
	- http://cglab.ca/~cdillaba/comp5008/mulmuley.html
	- http://www.cs.umd.edu/class/fall2014/cmsc754/Lects/lect09.pdf
	- class lecture notes
	
To Build: 
	- install apache ant and Java8 and make sure both are in path for host 
	  system (can be manually set in build/run scripts)
	- use build.bat (windows) or build.sh (linux)
To Run: 
	cd runScripts/Assignment3
	run_assignment3.(bat/sh) <input_file> [--ui]
	
Once computation is complete; the application will allow the user to insert 
matrix queries as x and y values separated by a space. The path through the 
graph will be printed for the user.

If the --ui flag is given (affter the input file), a JFrame will be displayed
containing the resulting trapezoidal map.

The adjacency graph will be outputted as assignment3.csv file at the end of 
the initial map construction. This can be found in the directory the assignment
was run from.

The adjacency graph is located in the directory along with this readme as 
adjacencyGraph.ods (LibreOffice) or adjacencyGraph.pdf (Acrobat). The 
trapezoidal map can be seen in map.png; also in this directory.

The labels are in a base-0 system (instead of starting at 1 like your example)
and the leaf nodes were not compacted once merged nodes were removed so they
are not explicitly linear. However, the labels in the adjacency graph and the
trapezoidal map do match.