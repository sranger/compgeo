Assignment 1
	Brute Force : http://algorithms4any1.blogspot.com/2013/02/convex-hull-part-2-brute-force-solution.html
	Jarvis      : http://en.wikipedia.org/wiki/Gift_wrapping_algorithm
	              http://www.personal.kent.edu/~rmuhamma/Compgeometry/MyCG/ConvexHull/jarvisMarch.htm

To Build: 
	- install ant and Java8 and make sure both are in path for host system (can be manually set in build/run scripts)
	- use build.bat (windows) or build.sh (linux)
To Run: 
	run_assignment1_bruteforce.(bat/sh) <number_of_points> [--ui]
	run_assignment1_jarvis.(bat/sh) <number_of_points> [--ui]

	if --ui is given at end of command, will display a graph of input points and the resulting convex hull

	each will output the points in counter-clockwise order in an "output_<algorithm>_<total_points>.txt" file.