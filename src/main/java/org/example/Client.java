package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {

    public static void main(String[] args) {
        Client client = new Client();
        Thread t1 = new Thread(client);
        t1.start();
    }

    @Override
    public void run() {
        try {
            Socket clientSocket = new Socket("localhost", 8667);
            TerminalUserInterface tui = new TerminalUserInterface(clientSocket);
            tui.start();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


}
