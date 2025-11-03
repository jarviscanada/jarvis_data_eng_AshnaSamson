package ca.jrvs.apps.grep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.BasicConfigurator;

public class JavaGrepImplementation implements JavaGrep {

    final Logger logger = LoggerFactory.getLogger(JavaGrep.class);

    private String regex;
    private String rootPath;
    private String outFile;

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("USAGE: JavaGrep regex rootPath outFile");
        }

        //Use default logger config
        BasicConfigurator.configure();

        JavaGrepImplementation javaGrep = new JavaGrepImplementation();
        javaGrep.setRegex(args[0]);
        javaGrep.setRootPath(args[1]);
        javaGrep.setOutFile(args[2]);

        try {
            javaGrep.process();
        } catch (Exception ex) {
            javaGrep.logger.error("Error: Unable to process", ex);
        }
    }

    /**
     * Top level search workflow
     * @throws IOException
     */
    @Override
    public void process() throws IOException {
        List matchedLines = new ArrayList<String>();
        for (File file : listFiles(getRootPath())) {
            for (String line : readLines(file)) {
                if (containsPattern(line)) {
                    matchedLines.add(line);
                }
            }
        }
        writeToFile(matchedLines);
    }

    /**
     * Traverse a given directory and return all files
     * @param rootDir input directory
     * @return files under rootDir
     */
    @Override
    public List<File> listFiles(String rootDir) {
        List<File> fileList = new ArrayList<File>();
        Queue<File> fileQueue = new LinkedList<File>();
        fileQueue.add(new File(rootDir));
        while (!fileQueue.isEmpty()) {
            File currentFile = fileQueue.remove();
            if (currentFile.isFile()) {
                fileList.add(currentFile);
            } else {
                for (File file : currentFile.listFiles()) {
                    fileQueue.add(file);
                }
            }
        }
        return fileList;
    }

    /**
     * Read a file and return all the lines
     *
     * Explain FileReader, BufferedReader, and character encoding
     *
     * @param inputFile file to be read
     * @return lines
     * @throws IllegalArgumentException if given an inputFile which is not a file
     */
    @Override
    public List<String> readLines(File inputFile) {
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException("Error: inputFile was not a file");
        }
        List<String> lineList = new ArrayList<String>();
        try (BufferedReader bufferReader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = bufferReader.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException ex) {
            logger.error("Failed to create buffered reader", ex);
        }
        return lineList;
    }

    /**
     * Check if a line contains the regex pattern (passed by the user)
     * @param line input string
     * @return true if there is a match
     */
    @Override
    public boolean containsPattern(String line) {
        Pattern pattern = Pattern.compile(this.getRegex());
        Matcher patternMatcher = pattern.matcher(line);
        return patternMatcher.matches();
    }

    /**
     * Write lines to a file
     *
     * Explore: FileOutputStream, OutputStreamWriter, and BufferedWriter
     *
     * @param lines matched lines
     * @throws IOException if write failed
     */
    @Override
    public void writeToFile(List<String> lines) throws IOException {
        File outputFile = new File(this.getOutFile());
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        try (BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for (String line : lines) {
                bufferWriter.write(line);
                bufferWriter.newLine();
            }
        } catch (IOException ex) {
            logger.error("Failed to create buffered writer", ex);
        }

    }

    @Override
    public String getRootPath() {
        return this.rootPath;
    }

    @Override
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String getRegex() {
        return this.regex;
    }

    @Override
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String getOutFile() {
        return this.outFile;
    }

    @Override
    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }
}