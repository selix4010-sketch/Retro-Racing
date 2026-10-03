package com.racing.main;

import com.racing.gui.VentanaPrincipal;
import javax.swing.SwingUtilities;

// Punto de entrada de la aplicacion
public class Main {
    public static void main(String[] args) {
        // Usamos invokeLater para asegurar que la interfaz grafica se construya 
        // en el hilo de despacho de eventos de Swing (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}