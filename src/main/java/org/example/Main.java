package org.example;

import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 8667);
            TerminalUserInterface tui = new TerminalUserInterface(clientSocket);
            tui.run();

        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }
}