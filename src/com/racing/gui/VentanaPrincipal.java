package com.racing.gui;

import com.racing.engine.PanelJuego;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VentanaPrincipal extends JFrame {

    private CardLayout layoutGestor;
    private JPanel panelContenedor;
    private PanelMenu panelMenu;
    private PanelJuego panelJuego;

    public VentanaPrincipal() {
        this.setTitle("Retro Racing");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.setSize(800, 800);
        this.setResizable(false); 
        this.setLocationRelativeTo(null); 

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        layoutGestor = new CardLayout();
        panelContenedor = new JPanel(layoutGestor);

        panelMenu = new PanelMenu(this);
        panelJuego = new PanelJuego(this);

        panelContenedor.add(panelMenu, "MENU");
        panelContenedor.add(panelJuego, "JUEGO");

        this.add(panelContenedor);
        
        panelMenu.reproducirMusica();
    }

    public void iniciarPartida(String rutaMapa, boolean modoDosJugadores) {
        panelMenu.detenerMusica(); 
        panelJuego.configurarPartida(rutaMapa, modoDosJugadores);
        layoutGestor.show(panelContenedor, "JUEGO");
        panelJuego.iniciarJuego();
        panelJuego.requestFocus();
    }

    public void iniciarPartidaConCarros(String rutaMapa, boolean esDosJugadores, int carro1Idx, int carro2Idx) {
        panelMenu.detenerMusica(); 
        panelJuego.configurarPartidaConCarros(rutaMapa, esDosJugadores, carro1Idx, carro2Idx);
        layoutGestor.show(panelContenedor, "JUEGO");
        panelJuego.iniciarJuego();
        panelJuego.requestFocus();
    }
    
    public void mostrarMenu() {
        panelJuego.detenerJuego(); 
        panelMenu.reproducirMusica(); 
        layoutGestor.show(panelContenedor, "MENU");
    }
    
    public void cambiarPanel(String nombre, JPanel panel) {
        panelContenedor.add(panel, nombre);
        layoutGestor.show(panelContenedor, nombre);
    }
}