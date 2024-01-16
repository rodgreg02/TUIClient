package org.example;

import java.io.*;
import java.time.LocalTime;

public class FileManager {
    File file = new File(LocalTime.now() + "log");
    FileWriter fileWriter;

    public FileManager() {
        try {
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String whatToWrite) throws IOException {
        fileWriter = new FileWriter(file, true);
        fileWriter.write(whatToWrite+"\n");
        fileWriter.flush();
        fileWriter.close();
    }
}
