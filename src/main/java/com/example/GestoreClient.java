package com.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GestoreClient extends Thread {
    private final Socket clientSocket;
    private final File cartellaDocumenti;

    public GestoreClient(Socket clientSocket, File cartellaDocumenti) {
        this.clientSocket = clientSocket;
        this.cartellaDocumenti = cartellaDocumenti;
    }

    @Override
    public void run() {
        try (Socket socket = this.clientSocket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("\nNuova connessione da: " + socket.getInetAddress() + " [" + getName() + "]");

            String richiesta = in.readLine();
            if (richiesta == null) {
                System.out.println("Client disconnesso senza inviare richieste.");
                return;
            }

            System.out.println("Richiesta ricevuta: [" + richiesta + "]");
            elaboraRichiesta(richiesta, out);

        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il client: " + e.getMessage());
        }
    }

    private void elaboraRichiesta(String richiesta, PrintWriter out) {
        String pulita = richiesta.trim();

        if (pulita.isEmpty()) {
            inviaErrore(out, "ERR SYNTAX");
            return;
        }

        String[] parti = pulita.split("\\s+");
        String comando = parti[0];

        if (!"LEGGI".equals(comando)) {
            inviaErrore(out, "ERR UNKNOWNCMD");
            return;
        }

        if (parti.length != 2 || !isNomeFileValido(parti[1])) {
            inviaErrore(out, "ERR SYNTAX");
            return;
        }

        String nomeFile = parti[1];
        File fileRichiesto = new File(cartellaDocumenti, nomeFile);

        if (!fileRichiesto.isFile()) {
            inviaErrore(out, "ERR NOTFOUND");
            return;
        }

        boolean statoInviato = false;
        try (BufferedReader lettoreFile = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileRichiesto), StandardCharsets.UTF_8))) {

            out.println("OK FILE");
            out.println();
            statoInviato = true;

            String riga;
            while ((riga = lettoreFile.readLine()) != null) {
                out.println(riga);
            }
            System.out.println("File '" + nomeFile + "' inviato con successo.");

        } catch (IOException e) {
            if (!statoInviato) {
                inviaErrore(out, "ERR READ");
            } else {
                System.err.println("Errore durante la lettura del file '" + nomeFile + "' a trasferimento iniziato: " + e.getMessage());
            }
        }
    }

    private boolean isNomeFileValido(String nome) {
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) {
            return false;
        }
        return nome.endsWith(".txt") && nome.length() > 4;
    }

    private void inviaErrore(PrintWriter out, String codiceErrore) {
        System.out.println("Risposta inviata: " + codiceErrore);
        out.println(codiceErrore);
        out.println();
    }
}
