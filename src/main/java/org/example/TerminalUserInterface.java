package org.example;

import com.googlecode.lanterna.SGR;
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
    int verticalAxis = 3;
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
            textGraphics.putString(0, 0, "Welcome to Mlack!");
            textGraphics.putString(0, 2, "Enter your username:");
            int amountOfChars = 0;
            while (!doneTyping) {
                KeyStroke ks = terminal.readInput();

                if (ks.getKeyType() != KeyType.Enter) {
                    textGraphics.putString(amountOfChars, 4, ks.getCharacter().toString());
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
            String string = inputStringBuilder.toString();
            printWriter.println("connect|" + string);
            textGraphics.putString(0, 6, "Your name is : " + string);
            terminal.flush();
            try {
                Thread.sleep(3000);
                terminal.clearScreen();
                terminal.flush();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
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
                textGraphics.putString(0,0,"Enter message: ");
                textGraphics.drawLine(0,1,100,1,'_');
                while ((message = bufferedReader.readLine()) != null) {
                    String[] splitMessage = message.split("\\|");
                    if (splitMessage[0].equals("new_message")) {
                        textGraphics.putString(0, verticalAxis, splitMessage[2] + " at " + splitMessage[1] + ":" + splitMessage[3]);
                        checkScroll();
                        verticalAxis++;
                        terminal.flush();
                    } else if (splitMessage[0].equals("new_user")) {
                        textGraphics.putString(0, verticalAxis, splitMessage[1] + " joined at " + splitMessage[2]);
                        checkScroll();
                        verticalAxis++;
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
                            textGraphics.setCharacter(amountOfChars, 0, ' ');
                            leftRight--;
                            amountOfChars--;
                            terminal.flush();
                        }else {
                            textGraphics.putString(leftRight, 0, ks.getCharacter().toString());
                            inputStringBuilder.append(ks.getCharacter());
                            amountOfChars++;
                            terminal.flush();
                            leftRight++;
                        }
                    }
                     else if (ks.getKeyType() == KeyType.Enter) {
                        doneTyping = true;
                    }
                }
                String string = inputStringBuilder.toString();
                inputStringBuilder.delete(0,amountOfChars);
                printWriter.println("send_message|" + string);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void checkScroll() {
        if (verticalAxis == 30) {
            verticalAxis = 3;
        }
    }
}
