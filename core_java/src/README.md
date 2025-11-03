# Introduction
This app is a Java implementation of the recursive `grep` function found on Linux systems. 
Specifically, given a root directory, a regex pattern to search by, and an output file, the app will recursively search through files in a directory and output all the lines that match a given regular expression, into the output file. 
This project served as an introduction to Java's regex classes (pattern and matcher), Java's I/O functions, Java 8's stream fuctionality and lambda implementation.

# Quick Start
Run the `main` function in JavaGrepImplementation with
the following arguments, in order:

Argument | Explanation
--- | ---
`regex` | The regex pattern to search through lines
`rootPath` | The path to the root directory to begin the search in
`outFile` | The file to output the search results into

## Sample implementation:

Running the app with the arguments `.*IllegalArgumentException.*`, `./grep/src`, and `/tmp/grep.out` would print
all the lines in every `.java` file in this project that contains `IllegalArgumentException` to the file `/tmp/grep.out`

## Pseudocode
Pseudocode for `process` is as follows:

```$java
matchedLines = []
for file in listFilesRecursively(rootDir)
  for line in readLines(file)
    if containsPattern(line)
      matchedLines.add(line)
writeToFile(matchedLines)
```

## Performance Issue
With the current implementations of this app, there will be an issue when particularly
large files are included in the root directory. Any file larger than the memory currently
assigned to the JVM running this app will cause performance issues such as `OutOfMemoryErrors`.
This makes the current approach less efficient for processing large datasets or log files.

# Test
I manually tested the application by preparing a sample directory containing multiple text files with known patterns. 
I ran the program using different regex inputs, such as searching for keywords like `"Exception"`, and verified that only the correct lines were written to the output file.

# Deployment
To simplify deployment and distribution, the application can be dockerized. 
A Dockerfile would be created to package the compiled Java application along with its dependencies into a lightweight container. 
This allows the app to run consistently across different environments. Once built, the Docker image can be run using a docker command.

# Improvement
1. Refactor the file reading logic to use streaming, allowing large files to be read line-by-line without loading the entire file into memory.
2. Include the ability to display line numbers alongside each matching line in the search results to improve tracebility.
3. Extend the tool to support additional Linux grep functionalities, such as case-insensitive search.