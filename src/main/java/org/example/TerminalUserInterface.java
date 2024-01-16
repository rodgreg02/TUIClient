package org.example;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TerminalUserInterface extends Thread {
    Socket clientSocket;
    Terminal terminal;
    TextGraphics textGraphics;
    int verticalAxis = 21;
    private PrintWriter printWriter;

    public TerminalUserInterface(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            boolean doneTyping = false;
            StringBuilder inputStringBuilder = new StringBuilder();
            printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            terminal = new DefaultTerminalFactory().createTerminal();
            textGraphics = terminal.newTextGraphics();
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
            textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
            textGraphics.putString(0, 0, "Welcome to MSlack!");
            textGraphics.putString(0, 1, "Enter your username:");
            int amountOfChars = 0;
            while (!doneTyping) {
                KeyStroke ks = terminal.readInput();

                if (ks.getKeyType() != KeyType.Enter && ks.getCharacter() != null) {
                    textGraphics.putString(amountOfChars, 2, ks.getCharacter().toString());
                    inputStringBuilder.append(ks.getCharacter());
                    amountOfChars++;
                    terminal.flush();
                }
                if (ks.getKeyType() == KeyType.Backspace) {
                    inputStringBuilder.deleteCharAt(amountOfChars);
                    textGraphics.setCharacter(amountOfChars, verticalAxis, ' ');
                    amountOfChars--;
                    terminal.flush();
                } else if (ks.getKeyType() == KeyType.Enter) {
                    doneTyping = true;
                }
            }
            String string;
            if (inputStringBuilder.isEmpty()) {
                string = "Im stupid";
            } else {
                string = inputStringBuilder.toString();
            }
            printWriter.println("connect|" + string);
            terminal.flush();
            terminal.clearScreen();
            terminal.flush();
            Thread readingThread = new Thread(this::startReading);
            Thread writingThread = new Thread(this::startWriting);
            readingThread.start();
            writingThread.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void startReading() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;
            while (true) {

                textGraphics.putString(0, 23, "Enter message: ");
                textGraphics.drawLine(14, 23, 100, 23, ' ');
                textGraphics.drawLine(0, 22, 100, 22, '_');
                terminal.flush();
                while ((message = bufferedReader.readLine()) != null) {
                    String[] splitMessage = message.split("\\|");
                        if (splitMessage[0].equals("new_message") || splitMessage[0].equals("new_user")) {
                            String displayMessage = "";
                            if (splitMessage[0].equals("new_message")) {
                                displayMessage = splitMessage[2] + " at " + splitMessage[1] + ":" + splitMessage[3];
                            }
                            if (splitMessage[0].equals("new_user")) {
                                displayMessage = splitMessage[1] + " joined at " + splitMessage[2];
                            }
                            if (verticalAxis < 21) {
                                textGraphics.putString(0, verticalAxis, displayMessage);
                                verticalAxis++;
                            } else {
                                for (int i = 1; i < 21; i++) {
                                    for (int j = 0; j < 50; j++) {
                                        textGraphics.setCharacter(j, i - 1, (textGraphics.getCharacter(j, i)));
                                    }
                                }
                                textGraphics.putString(0, 20, displayMessage + "                                                                      ");
                            }

                            terminal.flush();
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startWriting() {
        try {
            StringBuilder inputStringBuilder = new StringBuilder();
            while (true) {
                boolean doneTyping = false;
                printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                int leftRight = 15;
                int amountOfChars = 0;
                while (!doneTyping) {
                    KeyStroke ks = terminal.readInput();
                    if (ks.getKeyType() != KeyType.Enter) {
                        if (ks.getKeyType() == KeyType.Backspace) {
                            inputStringBuilder.deleteCharAt(amountOfChars - 1);
                            textGraphics.setCharacter(amountOfChars + 15 , 23, ' ');
                            leftRight--;
                            amountOfChars--;
                            terminal.flush();
                        } else {
                            textGraphics.putString(leftRight, 23, ks.getCharacter().toString());
                            inputStringBuilder.append(ks.getCharacter());
                            amountOfChars++;
                            terminal.flush();
                            leftRight++;
                        }
                    } else{
                        doneTyping = true;
                        textGraphics.putString(15,23,"                                                                     ");
                        terminal.flush();
                    }
                }
                String string;
                if (inputStringBuilder.isEmpty()) {
                    string = "Im fucking stupid";
                } else {
                    string = inputStringBuilder.toString();
                }
                inputStringBuilder.delete(0, amountOfChars);
                printWriter.println("send_message|" + string);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
