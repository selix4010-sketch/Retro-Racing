package com.racing.gui;

import com.racing.engine.PanelJuego;
import com.racing.engine.ServidorControl;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

// Capa base root del programa
public class VentanaPrincipal extends JFrame {

    // Usamos CardLayout para alternar "escenas" del juego sin abrir nuevas ventanas
    private CardLayout layoutGestor;
    private JPanel panelContenedor;
    
    // Instancias unicas de cada vista
    private PanelMenu panelMenu;
    private PanelJuego panelJuego;
    
    // Gateway del servidor web integrado
    private ServidorControl servidorCelular;

    public VentanaPrincipal() {
        this.setTitle("Retro Racing");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Limpia la RAM al cerrar 
        
        this.setSize(800, 800);
        this.setResizable(false); // Fija resolucion para no quebrar las fisicas que dependen de pixeles
        this.setLocationRelativeTo(null); 

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        layoutGestor = new CardLayout();
        panelContenedor = new JPanel(layoutGestor);

        panelJuego = new PanelJuego(this); 
        panelMenu = new PanelMenu(this);

        // Nombres logicos que sirven como ID de cada tarjeta/vista
        panelContenedor.add(panelMenu, "MENU");
        panelContenedor.add(panelJuego, "JUEGO");

        this.add(panelContenedor);
        
        // Levantamos el WebServer inyectandole la referencia local de la clase juego principal
        servidorCelular = new ServidorControl(panelJuego);
        servidorCelular.iniciar();
        
        panelMenu.reproducirMusica();
    }

    // Funciones enrutadoras de estados que encienden componentes logicos al cambiar tarjetas
    public void iniciarPartida(String rutaMapa, boolean modoDosJugadores) {
        panelMenu.detenerMusica(); 
        panelJuego.configurarPartida(rutaMapa, modoDosJugadores);
        layoutGestor.show(panelContenedor, "JUEGO");
        panelJuego.iniciarJuego();
        panelJuego.requestFocus(); // Focus obligatorio para escuchar inputs del teclado de PC
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