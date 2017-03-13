#Assignment
This is an interesting programming assignment whereby one is to decide whether a pair of subjects "meet" given some location data.

##System requirements
- Scala version 2.12.1
- Java 8

##Usage
1. Clone this directory
2. Put data file in `src/main/resources` folder
3. Run program using `sbt`

Example:
```
> sbt console

scala> Main.run(Array("600dfbe2","5e7b40e1"))
600dfbe2 and 5e7b40e1 meet on floor: 3, time: 2014-07-19T18:02:15.439Z, position: (103.31670697079016, 74.65082820932325) & position: (104.05757588027896, 75.6356891095315)
600dfbe2 and 5e7b40e1 meet on floor: 3, time: 2014-07-19T18:02:14.439Z, position: (103.31703914467367, 74.64799801256156) & position: (104.11410505349609, 75.19473090917458)
600dfbe2 and 5e7b40e1 meet on floor: 3, time: 2014-07-19T18:02:13.439Z, position: (103.31737131855718, 74.64516781579987) & position: (104.17063422671323, 74.75377270881765)
...
```

##Definitions & Assumptions
####What i assume to be "meet"
- The two subjects must be on the same floor in the same timespace i.e their timeline overlap
- The two subjects must be within 1 unit of x and y.
- Assuming the subjects are humans, i assume that their position won't change by much over a time window of 1 second (1000ms). This can be adjusted if the output is too verbose.

##Considerations
- I am using Scala 2.12.1 because of the convenience of `io.Source.fromResource`
- Adhere to a functional style of programming so that complex tasks can be broken down to several smaller functions and each of them can be coded and tested separately.
- We cannot trust that the data file will be of the correct format when given another set of supposedly data from the same source
- Timestamp format is in UTC time. I will set joda time default DateTimeZone to UTC time and only deal with UTC time throughout this assignment so that there will be no ambiguity with regards to time zone.

##Chain of Thought
- I need a component to read in the csv file and return an Iterator of case objects holding the data `CsvDataReader`
- I need a component to help filter down all the data to those that belongs to the 2 subjects in question `DataAccumulator`
- I need a component to process that relevant data and give a final result `DataProcessor`

##Complexity 
###Performance of the algorithm, i.e., space and time complexity
Because of the use of iterators for reading in data file and filtering down to those records that are relevant we can be rest assured that there will not be OutOfMemoryException. 
Having said that, there will still be OutOfMemoryException if the relevant records are so huge that the heap space runs out. We will then have to consider parallelizing or make use of a separate database storage to assist with this. 

It's always a balance between processing time and memory usage. We can use more processing time to recalculate information or use memory to save those calculations so that we don't have to recalculate them everytime its needed.
Caches could be used to 
- keep records belonging to 1 certain uid so that subsequent calls pertaining to the same uid do not have to look through the whole data file again.
- keep calculated position of subjects at a certain time on a certain floor 

##Future Work
###Room for Improvement
- Generalise the solution so that it works for multiple subjects instead of just two
- Create more case classes in replacement of the (bad) excessive use of Tuples for better code readability
- Lacking in integration tests, unit tets are not complete nor exhaustive
- Making several hard-coded variables configurable e.g. definition of "meet" being 1 meter apart; checking each subject's position every 1 second (1000ms) interval apart; data file name (reduced.csv)
- Looking at the output it is probably possible to have another step to do some kind of clustering to produce a "summary" of the final output
- If there was to be a large batch of queries, we can implement a queue (e.g. Amazon SQS) and several machines to process messages coming from that queue.
- If there was to be repeated queries, we can provide a REST api fronted by Nginx or Amazon API Gateway to do caching so that repeated queries of the same kind do not bog down the server
- If there was to be an infinite stream of input, i would consider having a component to do preprocessing of the data stream and then saving that output to a database which other components will tap on to do further processing