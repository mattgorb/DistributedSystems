Running:
Q1-Q9 are in JobOneReducer.java.  This is the default is the A3Job.java file.  
Q10 is in Q10Reducer.java.  Change the Reducer in the A3Job.java file to run
Also I split out the program into q1to6Reducer.java and q7to9Reducer.java.  

All Reducers use JobOneCombiner and JobOneMapper.

Question Explanations:
Question 7:
	For question 7 I just split the segment data by spaces and took the average at each index.  For example, for the third "start time" value, I took the value of the 3rd index for each song and averaged them out.  

Question 8:
	For question 8, I used the terms.  I figured out which were the most generic terms and unique terms.  A generic term is "rock".  I took the top 20 of the most generic terms and the most unique terms and counted how many times each artist had these values in their songs lists.  This determined whether an artist had a unique genre to describe them or not.  When I looked at my results, they made sense.  The generic artist had terms "rock rock singer-songwriter alternative rock country rock pop rock folk" etc, while the unique artist had "chanson trip hop big beat ballad j pop pop electro dub chill-out bossa nova".  I have never heard of the term "chanson".  

Question 9:
	To find a song that might have a higher hotness than the top song, I first found the top 25 songs overall.  From here, I computed their statistics such as danceability, mode, energy, key, loudness, etc.  For many of these values, simply taking the average of the top songs yielded me a good idea of what a better song might look like.  For other values, I had to find the most frequent value.  For example, for key, mode, and time signature, since their values are discrete, I computed the frequency of each and found the most frequent value among the top 25 songs.  From here, I named the band and song based off of my band when I was 14 years old (for fun).   

Question 10:
I wanted to find the top genres of each year.  To do this, I kept a list of the top 10 songs of each year, and found out their top genres among them.  I didn't have time to do anything cooler, but I wanted to see how they compared throughout the years, and how top genres have changed throughout the years.  Instead I just listed out the top 5 genres of each of the top 10 songs for each year.  

JobOneMapper.java
For the Mapper, I simply wrote the data to the combiner/reducer if it wasn't a header row.  Additionally, I had to set the key to the song ID and take into account the column of the song ID data for each type of file.  In the metadata, the song id was on column 8.  For analysis, it was on 1. 

JobOneCombiner.java
For the combiner, I combined the data from each analysis and metadata file into one long row but setting the songId to the first column and adding a "," after the end of the data so that I could append the next bit of data.  

JobOneReducer.java
This file contains all the questions and answers.  First, I reuse the combiner code to concatenate data between analysis and metadata files. Then, if the length of the csv data is greater than 33 columns after concatenation , I head into the reducer code.  I have different data structures for each question that hold data for various topics the reducer needs during cleanup to answer the question.  By far the longest part of the code is the cleanup section, when all my question are answered.  This uses the data structures that were 


Q10Reducer.java
Question 10 code. I compute a list of top 10 songs for each year and hold them in a data structure.  During cleanup, I use the data structures to list out the top terms for each of the top songs for each year. 

