# Automation hero interview challenge

### The task 

Given with a line separated text file of integers ranging anywhere from Integer.MIN to
Integer.MAX of size 1024MB, the program should be able to produce line separated text
file which has the sorted content of the input file.

Following preconditions:
* The program should be able to run with a memory constraint of 100MB i.e. the
-Xmx100m.
* The file can have duplicate integers.
* The text in the file has only integers which are line separated and no other
characters.

### Local testing and running ###
To run the program locally first please fill out the [properties file](automation-hero\src\main\resources\application.yml) in the application resources 
or define the following parameters: 
* EXTERNAL_MERGE_CHUNK_SIZE: size of the chunks used during merge sort (required only for merge sort)
* INPUT_FILE: input file path
* OUTPUT_FILE: output file path
* TEMP_OUTPUT_FILE: path of the temporary file to use during bubble sort. The file shouldn't exist before running the application and will be deleted after. (required only for bubble sort)
* TEMP_OUTPUT_FOLDER: path of the folder where the temporal chunks of the input file will be stored. The folder shouldn't exist before running the application and will be deleted after. (required only for merge sort)

After this the application is able to start with the default profile.
Please note that by default the external merge sort solution will be used. If you wish to change to external bubble sort please modify [AutomationHeroApplication.java](src\main\java\com\daniel\automationhero\AutomationHeroApplication.java)

The current properties are pointing to the resource folder where there are two example input files to test the application.

### Briefly about the problem and the solution(s) ###
The main issue is obvious; we can't load the whole file into JVM memory to sort it therefore using some sort of stream of the
file was necessary. I decided to use Java Streams as NiO has the ability to read a given file line-by-line and serve it as a form of stream.

That's fine, but what to do next?

Maybe a sorted stream could do the job, right? Well, no it couldn't. Java's Stream.sorted() is a nice thing if one wants to operate on a relatively
small stream, but the problem is that it kills the stream concept entirely as in the background it will create an ArrayList to store and sort the elements of the stream
and to do that it will load the content of the whole stream into memory.
Ok, so we have to use some sort of external sorting and temporary storing data in the given file system but which sorting algorithm would be fine in this case?
I implemented a sort of bubble sort as a bad example, and a merge sort to show the differences.

#### Bubble sort
Under the hood [this algorithm](src\main\java\com\daniel\automationhero\service\BubbleSortService.java) is using a temporary file, and the output file to comparing and storing the elements of the whole input file.
First it places the content of the input file to the temp file, opens a stream to the temp file, comparing the actual and the previous elements and writing the smaller one to the output file.
If the stream ends it will swap the functionality of the output, and the temp file and runs recursively until any swap happens between two neighbouring elements of the stream.
When the algorithm finishes it will remove the temp file.

Ok, so where is the catch?

The problem is that Bubble sort has a O(N^2) time complexity plus the File IO operations which also could be significant in this case. In case of  small (few hundred lines) files it could be ok, but if we wish to
sort a bigger file (several GBs) then it's going to take ages, but that's how square complexity works.

#### Merge sort
[Merge sort](src\main\java\com\daniel\automationhero\service\MergeSortService.java) takes a rather different approach. It streams the original file and creates K number of M sized or smaller (need to set this one in the properties file) chunks.
After this it will read all the chunks in separated streams and find the smallest element which will be written in the output file. This step will continue as long as there are elements in the streams.
The time complexity of the first step (divide and sort) is O(K*M*log M) as we need to iterate through every M sized chunks K times and using a default List.sort with time complexity of O(NlogN).
Finding the smallest element is O(K*M) as we need to iterate through K number of elements M times. 
The catch here is the chunk size. With too many chunks we have to open a lot of streams which means more memory consumption but if we open too few streams the sorting will take longer.
There is a goldilocks's zone of chunk sizes which this implementation simply ignores. Still, it's a much better approach for external sorting as a simple Bubble sort.

### Known issues ###
* The configurable chunk size could easily make tha application to consume much more memory as the limit described in the preconditions. Currently, the caller has to figure out a reasonable chunk size for a given file.
* Lack of tests. I spent a bit too much time on the implementation, and I didn't want to "cheat", so I created a very basic integration test for the Merge sort but that's all. After all there was a time limit and there are corners to cut, well it was one of those corners.

### Real life application
I know that the goal of this assignment was to implement an external sorting algorithm but in real a life situations I would'nt even start to do something like this. 
Sorting algorithms have a million shades and nuances to take care of, some many small details to optimize (just take the batch size as a rough example) and there are so many
already brewed, stable solutions to work with. In this case even Java seem to be an overkill as it would be easier to stream the file to its destination and call a `sort source_file > target_file` on it.
Or if we want to stick with Java there are many-many already implemented solutions for exactly this problem ([like this one](https://mvnrepository.com/artifact/com.google.code.externalsortinginjava/externalsortinginjava)).
Beside of it I really enjoyed this challenge!
