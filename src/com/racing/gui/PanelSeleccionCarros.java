package com.racing.gui;

import java.awt.*;
import javax.swing.*;

public class PanelSeleccionCarros extends JPanel {
    private VentanaPrincipal ventanaPrincipal;
    private boolean esDosJugadores;
    private int carroP1Seleccionado = 1;
    private int carroP2Seleccionado = 2;

    private JComboBox<String> comboP1;
    private JComboBox<String> comboP2;
    private JLabel lblPreviewP1;
    private JLabel lblPreviewP2;

    private final String[] nombresCarros = {
        "Carro 1 (Clásico / Azul)", 
        "Carro 2 (Deportivo / Rojo)", 
        "Carro 3 (Turbo / Verde)", 
        "Carro 4 (Rayo / Amarillo)"
    };

    public PanelSeleccionCarros(VentanaPrincipal ventana, boolean esDosJugadores) {
        this.ventanaPrincipal = ventana;
        this.esDosJugadores = esDosJugadores;
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("SELECCIÓN DE VEHÍCULOS", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 38));
        lblTitulo.setForeground(Color.YELLOW);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(60, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(1, esDosJugadores ? 2 : 1, 40, 0));
        panelCentro.setOpaque(false);
        // Márgenes laterales amplios para centrar los paneles
        panelCentro.setBorder(BorderFactory.createEmptyBorder(20, esDosJugadores ? 60 : 250, 40, esDosJugadores ? 60 : 250));

        panelCentro.add(crearPanelJugadorSelector("JUGADOR 1 (WASD)", 1, Color.CYAN));
        if (esDosJugadores) {
            panelCentro.add(crearPanelJugadorSelector("JUGADOR 2 (FLECHAS)", 2, Color.RED));
            carroP2Seleccionado = 2;
        }

        add(panelCentro, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);
        // Márgenes grandes a los lados para que el botón no sea excesivamente largo
        panelInferior.setBorder(BorderFactory.createEmptyBorder(20, 220, 80, 220));

        JButton btnComenzar = new JButton("¡LISTOS... FUERA!");
        btnComenzar.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 28));
        btnComenzar.setBackground(new Color(0, 0, 150));
        btnComenzar.setForeground(Color.WHITE);
        btnComenzar.setFocusPainted(false);
        btnComenzar.setPreferredSize(new Dimension(0, 70));
        btnComenzar.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
        btnComenzar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnComenzar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnComenzar.setBackground(Color.YELLOW);
                btnComenzar.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnComenzar.setBackground(new Color(0, 0, 150));
                btnComenzar.setForeground(Color.WHITE);
            }
        });
        
        btnComenzar.addActionListener(e -> {
            if (esDosJugadores && carroP1Seleccionado == carroP2Seleccionado) {
                JOptionPane.showMessageDialog(this, 
                    "¡Los jugadores no pueden elegir el mismo carro!", 
                    "Conflicto de Selección", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            ventanaPrincipal.iniciarPartidaConCarros("/com/racing/res/pngwing.com.png", esDosJugadores, carroP1Seleccionado, carroP2Seleccionado);
        });
        
        panelInferior.add(btnComenzar, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelJugadorSelector(String titulo, int jugadorID, Color colorBorde) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0, 0, 30, 180));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorBorde, 4),
            BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));

        JLabel lblJugador = new JLabel(titulo);
        lblJugador.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblJugador.setForeground(Color.WHITE);
        lblJugador.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInstruccion = new JLabel("Elige tu diseño:");
        lblInstruccion.setFont(new Font("Monospaced", Font.PLAIN, 16));
        lblInstruccion.setForeground(Color.LIGHT_GRAY);
        lblInstruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPreview = new JLabel();
        lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPreview.setPreferredSize(new Dimension(100, 160));

        JComboBox<String> combo = new JComboBox<>(nombresCarros);
        combo.setFont(new Font("Arial", Font.BOLD, 15));
        combo.setPreferredSize(new Dimension(240, 40)); 
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);

        // Envase (FlowLayout) para evitar que el JComboBox se estire horizontalmente
        JPanel panelComboContenedor = new JPanel();
        panelComboContenedor.setOpaque(false);
        panelComboContenedor.add(combo);

        if (jugadorID == 1) {
            comboP1 = combo;
            lblPreviewP1 = lblPreview;
            actualizarImagenPreview(lblPreviewP1, 1);
        } else {
            comboP2 = combo;
            lblPreviewP2 = lblPreview;
            comboP2.setSelectedIndex(1); 
            actualizarImagenPreview(lblPreviewP2, 2);
        }

        combo.addActionListener(e -> {
            int idx = combo.getSelectedIndex() + 1;
            
            if (esDosJugadores && comboP1 != null && comboP2 != null) {
                if (jugadorID == 1 && idx == carroP2Seleccionado) {
                    JOptionPane.showMessageDialog(this, "¡Este carro ya ha sido seleccionado por el Jugador 2!", "Vehículo ocupado", JOptionPane.WARNING_MESSAGE);
                    comboP1.setSelectedIndex(carroP1Seleccionado - 1);
                    return;
                } else if (jugadorID == 2 && idx == carroP1Seleccionado) {
                    JOptionPane.showMessageDialog(this, "¡Este carro ya ha sido seleccionado por el Jugador 1!", "Vehículo ocupado", JOptionPane.WARNING_MESSAGE);
                    comboP2.setSelectedIndex(carroP2Seleccionado - 1);
                    return;
                }
            }

            if (jugadorID == 1) {
                carroP1Seleccionado = idx;
                actualizarImagenPreview(lblPreviewP1, idx);
            } else {
                carroP2Seleccionado = idx;
                actualizarImagenPreview(lblPreviewP2, idx);
            }
        });

        panel.add(lblJugador);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblInstruccion);
        panel.add(Box.createVerticalStrut(25));
        panel.add(lblPreview);
        panel.add(Box.createVerticalStrut(25));
        panel.add(panelComboContenedor); 

        return panel;
    }

    private void actualizarImagenPreview(JLabel labelPrevisualizacion, int tipoCarro) {
        String ruta = "/com/racing/res/carro" + tipoCarro + ".png";
        try {
            ImageIcon iconoOriginal = new ImageIcon(getClass().getResource(ruta));
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
            labelPrevisualizacion.setIcon(new ImageIcon(imagenEscalada));
            labelPrevisualizacion.setText("");
        } catch (Exception e) {
            labelPrevisualizacion.setIcon(null);
            labelPrevisualizacion.setText("[ Sin Imagen ]");
            labelPrevisualizacion.setForeground(Color.RED);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        
        GradientPaint gp = new GradientPaint(0, 0, new Color(0, 0, 20), 0, h, new Color(0, 80, 255));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }
}