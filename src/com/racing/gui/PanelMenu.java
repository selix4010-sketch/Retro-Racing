package com.racing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class PanelMenu extends JPanel {

    private VentanaPrincipal ventanaPadre;
    private Clip clipMusicaMenu;
    private boolean mostrarTextoParpadeante = true;
    private Timer timerParpadeo;

    public PanelMenu(VentanaPrincipal ventanaPadre) {
        this.ventanaPadre = ventanaPadre;
        this.setLayout(new BorderLayout());

        // Se restauraron los márgenes anteriores (EmptyBorder 380, 200, 100, 200)
        JPanel panelControles = new JPanel(new GridLayout(3, 1, 15, 20));
        panelControles.setOpaque(false);
        panelControles.setBorder(new EmptyBorder(380, 200, 100, 200)); 

        JButton btnVsPC = crearBotonRetro("CONTRA PC");
        btnVsPC.addActionListener(e -> iniciarSeleccion(false));
        panelControles.add(btnVsPC);

        JButton btnDosJugadores = crearBotonRetro("DOS JUGADORES");
        btnDosJugadores.addActionListener(e -> iniciarSeleccion(true));
        panelControles.add(btnDosJugadores);

        JButton btnManual = crearBotonRetro("MANUAL DE USUARIO");
        btnManual.addActionListener(e -> mostrarManualUsuario());
        panelControles.add(btnManual);

        this.add(panelControles, BorderLayout.CENTER);

        timerParpadeo = new Timer(500, e -> {
            mostrarTextoParpadeante = !mostrarTextoParpadeante;
            repaint();
        });
        timerParpadeo.start();
    }

    private JButton crearBotonRetro(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 22));
        btn.setBackground(new Color(0, 0, 150)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.YELLOW);
                btn.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 0, 150));
                btn.setForeground(Color.WHITE);
            }
        });
        return btn;
    }

    private void iniciarSeleccion(boolean dosJugadores) {
        PanelSeleccionCarros panelSelector = new PanelSeleccionCarros(ventanaPadre, dosJugadores);
        ventanaPadre.cambiarPanel("SELECTOR_CARROS", panelSelector);
    }

    public void reproducirMusica() {
        try {
            if (clipMusicaMenu != null && clipMusicaMenu.isRunning()) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/com/racing/res/musicamenu.wav"));
            clipMusicaMenu = AudioSystem.getClip();
            clipMusicaMenu.open(ais);
            clipMusicaMenu.loop(Clip.LOOP_CONTINUOUSLY);
            clipMusicaMenu.start();
        } catch (Exception e) {}
    }

    public void detenerMusica() {
        if (clipMusicaMenu != null && clipMusicaMenu.isRunning()) {
            clipMusicaMenu.stop();
            clipMusicaMenu.close();
        }
    }

    private void mostrarManualUsuario() {
        JOptionPane.showMessageDialog(this,
            "=================== MANUAL DE USUARIO ===================\n\n" +
            "1. OBJETIVO:\n" +
            "   Completa 3 vueltas al circuito antes que tus rivales evitando chocar.\n\n" +
            "2. CONTROLES:\n" +
            "   - Jugador 1: Teclas W (Acelerar), S (Frenar), A/D (Girar).\n" +
            "   - Jugador 2: Flechas Direccionales (Arriba, Abajo, Izquierda, Derecha).\n\n" +
            "3. SISTEMA DE PUNTOS Y REVANCHA:\n" +
            "   - Las victorias se acumulan en el marcador global de la partida.\n" +
            "   - Al terminar la carrera, presiona [ENTER] para jugar una REVANCHA\n" +
            "     inmediata manteniendo la puntuación acumulada.\n" +
            "   - Presiona [ESC] para salir al menú principal.\n\n" +
            "4. ADVERTENCIA DE SENTIDO CONTRARIO:\n" +
            "   - Si conduces en dirección opuesta a la pista, el sistema emitirá\n" +
            "     una advertencia visual en pantalla.",
            "Manual de Usuario - Retro Racing",
            JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        GradientPaint fondoGp = new GradientPaint(0, 0, new Color(0, 0, 20), 0, h, new Color(0, 80, 255));
        g2.setPaint(fondoGp);
        g2.fillRect(0, 0, w, h);

        g2.setColor(Color.WHITE);
        g2.fillRect(150, 80, 3, 3);
        g2.fillRect(680, 100, 4, 4);
        g2.setColor(Color.YELLOW);
        g2.fillRect(200, 250, 3, 3);
        g2.fillRect(600, 280, 2, 2);

        Font fuenteTitulo = new Font("SansSerif", Font.BOLD | Font.ITALIC, 85);
        g2.setFont(fuenteTitulo);
        FontMetrics fm = g2.getFontMetrics();

        String txt1 = "RETRO";
        String txt2 = "RACING";
        
        int x1 = (w - fm.stringWidth(txt1)) / 2 - 20; 
        int y1 = 150;
        int x2 = (w - fm.stringWidth(txt2)) / 2 + 30; 
        int y2 = 250; 

        g2.setColor(Color.WHITE);
        g2.drawString(txt1, x1 - 3, y1 - 3);
        g2.drawString(txt1, x1 + 3, y1 + 3);
        g2.drawString(txt2, x2 - 3, y2 - 3);
        g2.drawString(txt2, x2 + 3, y2 + 3);

        g2.setColor(Color.BLACK);
        g2.drawString(txt1, x1 + 8, y1 + 8);
        g2.drawString(txt2, x2 + 8, y2 + 8);

        GradientPaint textoGp = new GradientPaint(0, 50, Color.RED, 0, 260, Color.YELLOW);
        g2.setPaint(textoGp);
        g2.drawString(txt1, x1, y1);
        g2.drawString(txt2, x2, y2);

        if (mostrarTextoParpadeante) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2.setColor(Color.YELLOW);
            String press = "SELECCIONA TU MODO DE JUEGO";
            int px = (w - g2.getFontMetrics().stringWidth(press)) / 2;
            g2.drawString(press, px, 330);
        }
    }
}