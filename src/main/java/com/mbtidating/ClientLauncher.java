package com.mbtidating;

import com.mbtidating.view.MainApp;

import javax.swing.*;

public class ClientLauncher {

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
            System.out.println("🟢 Client UI Started");
        });
    }

    public static void main(String[] args) {
        start();
    }
}
