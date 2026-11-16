package com.example;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int PORTA = 3000;
    private static final String NOME_CARTELLA = "documenti";

    public static void main(String[] args) {
        File cartellaDocumenti = new File(NOME_CARTELLA);
        System.out.println("Avvio server multithread sulla porta " + PORTA + "...");
        System.out.println("Cartella documenti cercata in: " + cartellaDocumenti.getAbsolutePath());

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                GestoreClient gestore = new GestoreClient(clientSocket, cartellaDocumenti);
                gestore.start();
            }
        } catch (IOException e) {
            System.err.println("Impossibile avviare il server sulla porta " + PORTA + ": " + e.getMessage());
        }
    }
}