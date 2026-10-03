package com.racing.engine;

import com.racing.carros.Carro;
import com.racing.gui.VentanaPrincipal;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.util.ArrayList;

// Motor central de la aplicacion que dibuja la pista y coordina todos los objetos en un hilo separado
public class PanelJuego extends JPanel implements Runnable, KeyListener {

    private VentanaPrincipal ventanaPadre;
    private Thread hiloPrincipal;
    private boolean enEjecucion;
    private final int FPS = 60; // Tasa de refresco objetivo

    // Recursos graficos
    private BufferedImage imagenMapa;
    private Image imagenFondoPista; 
    
    // Lista dinamica de todos los objetos renderizables en pista
    private ArrayList<Carro> vehiculos;
    private Carro jugador1;
    private Carro jugador2;
    private Carro carroIA1, carroIA2, carroIA3;
    private boolean modoDosJugadores = false;
    
    // Marcadores globales estaticos para no perder los puntos al reiniciar la partida
    private static int puntosP1 = 0;
    private static int puntosP2 = 0;
    private static int puntosCPU = 0;
    
    // Rectangulos de interaccion invisibles
    private Rectangle zonaMeta;
    private Rectangle zonaCheckpoint;

    // Control de flujo del juego
    private int framesConteo;
    private boolean carreraIniciada;
    private boolean carreraTerminada;
    private boolean premioEntregado = false;
    private String nombreGanador;
    private final int VUELTAS_PARA_GANAR = 3;

    private String ultimaRutaMapa = "/com/racing/res/pngwing.com.png";
    private int ultimoCarroP1 = 1;
    private int ultimoCarroP2 = 2;

    // Recursos de audio
    private Clip clipMusica;
    private long ultimoSonidoChoque = 0; // Cooldown para evitar saturacion de sonido
    
    private Font fuenteExacta;

    public PanelJuego(VentanaPrincipal ventanaPadre) {
        this.ventanaPadre = ventanaPadre;
        this.setDoubleBuffered(true); // Evita el parpadeo grafico (flickering) al dibujar
        this.setBackground(new Color(34, 139, 34)); 
        this.setFocusable(true);
        this.addKeyListener(this); // Escucha las entradas del teclado
        
        cargarFuentePersonalizada();

        // Carga la imagen de fondo visual
        try {
            imagenFondoPista = new ImageIcon(getClass().getResource("/com/racing/res/pngwing.com.png")).getImage();
        } catch (Exception e) {
            imagenFondoPista = null;
        }
    }
    
    // Carga la tipografia retro, usa Arial como respaldo si el archivo falta
    private void cargarFuentePersonalizada() {
        try {
            InputStream is = getClass().getResourceAsStream("/com/racing/res/topgear.ttf");
            if (is != null) {
                fuenteExacta = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                fuenteExacta = new Font("Arial", Font.BOLD, 20);
            }
        } catch (Exception e) {
            fuenteExacta = new Font("Arial", Font.BOLD, 20);
        }
    }
    
    // Inicializa una partida predeterminada (usado como respaldo)
    public void configurarPartida(String rutaMapa, boolean modoDosJugadores) {
        this.modoDosJugadores = modoDosJugadores;
        this.ultimaRutaMapa = rutaMapa;
        try {
            imagenMapa = ImageIO.read(getClass().getResourceAsStream(rutaMapa));
        } catch (Exception e) {
            System.err.println("Error: No se encontró " + rutaMapa);
        }
        
        // 180 frames a 60FPS equivalen exactamente a 3 segundos de cuenta regresiva
        framesConteo = 180; 
        carreraIniciada = false;
        carreraTerminada = false;
        premioEntregado = false;
        nombreGanador = "";

        vehiculos = new ArrayList<>();
        
        zonaMeta = new Rectangle(65, 280, 80, 20); 
        zonaCheckpoint = new Rectangle(630, 300, 100, 200); 

        // Instancia a los jugadores y la computadora dependiendo del modo seleccionado
        if (modoDosJugadores) {
            jugador1 = new Carro(85, 245, 90, "/com/racing/res/carro1.png", false, Color.BLUE, "JUGADOR 1");
            jugador2 = new Carro(125, 245, 90, "/com/racing/res/carro2.png", false, Color.RED, "JUGADOR 2");
            vehiculos.add(jugador1);
            vehiculos.add(jugador2);
            carroIA1 = carroIA2 = carroIA3 = null;
        } else {
            jugador1 = new Carro(85, 245, 90, "/com/racing/res/carro1.png", false, Color.BLUE, "JUGADOR 1");
            jugador2 = null;
            carroIA1 = new Carro(125, 245, 90, "/com/racing/res/carro2.png", true, Color.CYAN, "CPU 1");
            carroIA2 = new Carro(85, 200, 90, "/com/racing/res/carro3.png", true, Color.GREEN, "CPU 2");
            carroIA3 = new Carro(125, 200, 90, "/com/racing/res/carro4.png", true, Color.YELLOW, "CPU 3");
            
            vehiculos.add(jugador1);
            vehiculos.add(carroIA1);
            vehiculos.add(carroIA2);
            vehiculos.add(carroIA3);
        }
        reproducirMusicaCarrera();
    }

    // Inicializa la partida cargando los sprites que eligieron los usuarios en el menu anterior
    public void configurarPartidaConCarros(String rutaMapa, boolean modoDosJugadores, int carro1Idx, int carro2Idx) {
        this.modoDosJugadores = modoDosJugadores;
        this.ultimaRutaMapa = rutaMapa;
        this.ultimoCarroP1 = carro1Idx;
        this.ultimoCarroP2 = carro2Idx;

        try {
            imagenMapa = ImageIO.read(getClass().getResourceAsStream(rutaMapa));
        } catch (Exception e) {
            System.err.println("Error: No se encontró " + rutaMapa);
        }
        
        framesConteo = 180; 
        carreraIniciada = false;
        carreraTerminada = false;
        premioEntregado = false;
        nombreGanador = "";

        vehiculos = new ArrayList<>();
        
        zonaMeta = new Rectangle(65, 280, 80, 20); 
        zonaCheckpoint = new Rectangle(630, 300, 100, 200); 

        String rutaSpriteP1 = "/com/racing/res/carro" + carro1Idx + ".png";

        if (modoDosJugadores) {
            String rutaSpriteP2 = "/com/racing/res/carro" + carro2Idx + ".png";
            jugador1 = new Carro(85, 245, 90, rutaSpriteP1, false, Color.BLUE, "JUGADOR 1");
            jugador2 = new Carro(125, 245, 90, rutaSpriteP2, false, Color.RED, "JUGADOR 2");
            vehiculos.add(jugador1);
            vehiculos.add(jugador2);
            carroIA1 = carroIA2 = carroIA3 = null;
        } else {
            jugador1 = new Carro(85, 245, 90, rutaSpriteP1, false, Color.BLUE, "JUGADOR 1");
            jugador2 = null;
            carroIA1 = new Carro(125, 245, 90, "/com/racing/res/carro2.png", true, Color.CYAN, "CPU 1");
            carroIA2 = new Carro(85, 200, 90, "/com/racing/res/carro3.png", true, Color.GREEN, "CPU 2");
            carroIA3 = new Carro(125, 200, 90, "/com/racing/res/carro4.png", true, Color.YELLOW, "CPU 3");
            
            vehiculos.add(jugador1);
            vehiculos.add(carroIA1);
            vehiculos.add(carroIA2);
            vehiculos.add(carroIA3);
        }
        reproducirMusicaCarrera();
    }

    // Gestion de audio via Clip para reproduccion continua sin bloqueos
    private void reproducirMusicaCarrera() {
        try {
            detenerMusica();
            AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/com/racing/res/musica1.wav"));
            clipMusica = AudioSystem.getClip();
            clipMusica.open(ais);
            clipMusica.loop(Clip.LOOP_CONTINUOUSLY);
            clipMusica.start();
        } catch (Exception e) {}
    }

    public void detenerMusica() {
        if (clipMusica != null && clipMusica.isRunning()) {
            clipMusica.stop();
            clipMusica.close();
        }
    }

    // Emite sonido si no se ha reproducido uno en los ultimos 500 milisegundos
    private void reproducirSonidoChoque() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimoSonidoChoque > 500) { 
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/com/racing/res/choque.wav"));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
                ultimoSonidoChoque = tiempoActual;
            } catch (Exception e) {}
        }
    }

    // Inicializa el bucle de animacion principal
    public void iniciarJuego() {
        if (hiloPrincipal == null || !enEjecucion) {
            hiloPrincipal = new Thread(this);
            enEjecucion = true;
            hiloPrincipal.start();
        }
    }
    
    public void detenerJuego() {
        enEjecucion = false;
        detenerMusica();
    }

    // El corazon del juego: Arquitectura Delta Time para asegurar que siempre corra
    // a la misma velocidad independientemente de la potencia de la computadora
    @Override
    public void run() {
        double tiempoPorFrame = 1000000000.0 / FPS;
        double delta = 0;
        long tiempoAnterior = System.nanoTime();

        while (enEjecucion) {
            long tiempoActual = System.nanoTime();
            delta += (tiempoActual - tiempoAnterior) / tiempoPorFrame;
            tiempoAnterior = tiempoActual;

            if (delta >= 1) {
                // Cuenta regresiva antes de empezar a mover los carros
                if (!carreraIniciada && !carreraTerminada) {
                    framesConteo--;
                    if (framesConteo <= 0) carreraIniciada = true;
                }

                // Se procesan las fisicas de todos los carros presentes
                for (Carro c : vehiculos) {
                    c.actualizar(imagenMapa, zonaMeta, zonaCheckpoint, (carreraIniciada && !carreraTerminada), this.getWidth(), this.getHeight());
                    
                    // Comprobacion de victoria
                    if (c.getVueltas() >= VUELTAS_PARA_GANAR && !carreraTerminada) {
                        carreraTerminada = true;
                        nombreGanador = c.getNombre();
                        
                        // Solo se asigna el punto a la primera persona que cruza
                        if (!premioEntregado) {
                            if (c.getNombre().equals("JUGADOR 1")) puntosP1++;
                            else if (c.getNombre().equals("JUGADOR 2")) puntosP2++;
                            else puntosCPU++;
                            premioEntregado = true;
                        }
                    }
                }
                
                // Checa que los carros no esten superponiendose entre si
                if (carreraIniciada && !carreraTerminada) {
                    verificarColisionesAutos();
                }
                
                repaint(); // Manda a llamar a paintComponent para dibujar el frame
                delta--;
            }
        }
    }

    // Algoritmo de deteccion de colisiones circulares entre cada par de vehiculos
    private void verificarColisionesAutos() {
        for (int i = 0; i < vehiculos.size(); i++) {
            for (int j = i + 1; j < vehiculos.size(); j++) {
                Carro c1 = vehiculos.get(i);
                Carro c2 = vehiculos.get(j);
                
                // Teorema de Pitagoras para sacar la hipotenusa (distancia)
                double dx = c2.getX() - c1.getX();
                double dy = c2.getY() - c1.getY();
                double distancia = Math.sqrt(dx * dx + dy * dy);
                double radioColision = 20.0; 

                // Si la distancia es menor al diametro combinado, chocaron
                if (distancia < radioColision && distancia > 0) {
                    double superposicion = radioColision - distancia;
                    
                    // Se calcula un vector de empuje inverso para separarlos equitativamente
                    double empujeX = (dx / distancia) * superposicion * 0.2;
                    double empujeY = (dy / distancia) * superposicion * 0.2;

                    c1.setX(c1.getX() - empujeX);
                    c1.setY(c1.getY() - empujeY);
                    c2.setX(c2.getX() + empujeX);
                    c2.setY(c2.getY() + empujeY);
                    
                    reproducirSonidoChoque();
                }
            }
        }
    }

    // Renderizado en pantalla de todas las capas visuales
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = this.getWidth();
        int alto = this.getHeight();

        // 1. Dibujamos el escenario base (cesped, gradas, decoracion)
        dibujarFondoEstadioF1(g2d, ancho, alto);

        // 2. Dibujamos la pista real encima
        if (imagenFondoPista != null) {
            g2d.drawImage(imagenFondoPista, 0, 0, ancho, alto, this);
        }

        // 3. (Opcional) Dibuja la mascara de colision si se habilita para debug
        if (imagenMapa != null) {
            // g2d.drawImage(imagenMapa, 0, 0, ancho, alto, null);
        }
        
        // 4. Dibujamos la meta usando bucles de cuadros de ajedrez
        g2d.setColor(Color.WHITE);
        g2d.fill(zonaMeta);
        g2d.setColor(Color.BLACK);
        for (int i = zonaMeta.x; i < zonaMeta.x + zonaMeta.width; i += 10) {
            g2d.fillRect(i, zonaMeta.y, 5, 10);
            g2d.fillRect(i + 5, zonaMeta.y + 10, 5, 10);
        }

        // 5. Dibujamos las entidades (carros)
        for (Carro c : vehiculos) {
            c.dibujar(g2d);
        }
        
        // 6. UI: Head Up Display (HUD) con marcador
        g2d.setColor(new Color(15, 19, 22, 210)); 
        g2d.fillRoundRect(20, 20, 310, modoDosJugadores ? 90 : 80, 12, 12);
        g2d.setColor(new Color(241, 196, 15)); 
        g2d.drawRoundRect(20, 20, 310, modoDosJugadores ? 90 : 80, 12, 12);

        g2d.setColor(Color.WHITE);
        g2d.setFont(fuenteExacta.deriveFont(Font.PLAIN, 15f));
        g2d.drawString("J1 Vueltas  " + Math.min(jugador1.getVueltas(), VUELTAS_PARA_GANAR) + "I" + VUELTAS_PARA_GANAR + "  I Pts " + puntosP1, 30, 40);
        
        if (modoDosJugadores && jugador2 != null) {
            g2d.drawString("J2 Vueltas  " + Math.min(jugador2.getVueltas(), VUELTAS_PARA_GANAR) + "I" + VUELTAS_PARA_GANAR + " I Pts " + puntosP2, 30, 65);
            g2d.setFont(fuenteExacta.deriveFont(Font.PLAIN, 12f));
            g2d.drawString("ESC Menu  I  ENTER Revancha", 30, 95);
        } else {
            g2d.drawString("CPU Pts " + puntosCPU, 30, 62);
            g2d.setFont(fuenteExacta.deriveFont(Font.PLAIN, 12f));
            g2d.drawString("ESC Menu  I  ENTER Revancha", 30, 85);
        }

        // UI: Advertencia de conductor suicida
        if (!carreraTerminada && carreraIniciada) {
            String alertaTexto = "";
            if (jugador1.vaEnSentidoContrario()) {
                alertaTexto = "ADVERTENCIA J1 SENTIDO CONTRARIO";
            } else if (modoDosJugadores && jugador2 != null && jugador2.vaEnSentidoContrario()) {
                alertaTexto = "ADVERTENCIA J2 SENTIDO CONTRARIO";
            }

            if (!alertaTexto.isEmpty()) {
                g2d.setColor(new Color(231, 76, 60, 220));
                g2d.fillRect(ancho / 2 - 250, 40, 500, 40);
                g2d.setColor(Color.WHITE);
                g2d.setFont(fuenteExacta.deriveFont(Font.PLAIN, 18f));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(alertaTexto, (ancho - fm.stringWidth(alertaTexto)) / 2, 67);
            }
        }

        // UI: Texto flotante centralizado (Conteo o Victoria)
        if (!carreraIniciada && !carreraTerminada) {
            int segundos = (framesConteo / 60) + 1;
            String textoCentro = framesConteo < 20 ? "GO!" : String.valueOf(segundos);
            Color colorTexto = framesConteo < 20 ? new Color(46, 204, 113) : Color.YELLOW;
            dibujarTextoCentrado(g2d, textoCentro, fuenteExacta.deriveFont(Font.PLAIN, 130f), colorTexto, 0);
        }

        if (carreraTerminada) {
            g2d.setColor(new Color(0, 0, 0, 210)); 
            g2d.fillRect(0, 0, ancho, alto);
            
            dibujarTextoCentrado(g2d, nombreGanador + " GANA!", fuenteExacta.deriveFont(Font.PLAIN, 65f), new Color(46, 204, 113), -70);
            
            g2d.setFont(fuenteExacta.deriveFont(Font.PLAIN, 24f));
            g2d.setColor(Color.YELLOW);
            String marcadorTxt = "MARCADOR  J1 " + puntosP1 + (modoDosJugadores ? " I J2 " + puntosP2 : " I CPU " + puntosCPU);
            int xMarcador = (ancho - g2d.getFontMetrics().stringWidth(marcadorTxt)) / 2;
            g2d.drawString(marcadorTxt, xMarcador, alto / 2 - 10);

            g2d.setColor(Color.WHITE);
            dibujarTextoCentrado(g2d, "Presiona ENTER para REVANCHA", fuenteExacta.deriveFont(Font.PLAIN, 20f), Color.CYAN, 40);
            dibujarTextoCentrado(g2d, "Presiona ESC para el Menu Principal", fuenteExacta.deriveFont(Font.PLAIN, 16f), Color.LIGHT_GRAY, 80);
        }
        
        g2d.dispose();
    }

    // Metodo helper para decorar los bordes de la pista para que no se vea tan simple
    private void dibujarFondoEstadioF1(Graphics2D g2, int ancho, int alto) {
        g2.setColor(new Color(46, 125, 50));
        g2.fillRect(0, 0, ancho, alto);

        g2.setColor(new Color(189, 195, 199)); 
        g2.fillRect(50, 15, 700, 30);
        g2.fillRect(50, 745, 700, 30);

        g2.setColor(new Color(44, 62, 80));
        g2.fillRect(40, 5, 720, 12);
        g2.fillRect(40, 775, 720, 12);

        Color[] coloresGente = {Color.RED, Color.BLUE, Color.YELLOW, Color.WHITE, Color.ORANGE, Color.CYAN, Color.MAGENTA};
        for (int x = 60; x < 740; x += 10) {
            for (int y = 20; y < 40; y += 8) {
                int colorIndex = (x + y) % coloresGente.length;
                g2.setColor(coloresGente[colorIndex]);
                g2.fillRect(x, y, 4, 5);
                g2.fillRect(x, y + 735, 4, 5);
            }
        }

        int[][] posicionesArboles = {
            {20, 100}, {20, 250}, {20, 400}, {20, 550}, {20, 700},
            {760, 100}, {760, 250}, {760, 400}, {760, 550}, {760, 700},
            {350, 20}, {500, 20}, {350, 750}, {500, 750},
            {150, 680}, {650, 680}, {150, 80}, {650, 80}
        };

        for (int[] pos : posicionesArboles) {
            g2.setColor(new Color(121, 85, 72));
            g2.fillRect(pos[0] - 4, pos[1] - 4, 8, 12);
            g2.setColor(new Color(27, 94, 32));
            g2.fillOval(pos[0] - 16, pos[1] - 22, 32, 28);
            g2.setColor(new Color(46, 125, 50));
            g2.fillOval(pos[0] - 12, pos[1] - 18, 24, 20);
        }
    }

    // Utileria para calcular metricas y centrar un String perfectamente con su sombra negra
    private void dibujarTextoCentrado(Graphics2D g2, String texto, Font fuente, Color color, int offsetY) {
        g2.setFont(fuente);
        FontMetrics fm = g2.getFontMetrics();
        int x = (this.getWidth() - fm.stringWidth(texto)) / 2;
        int y = (this.getHeight() - fm.getHeight()) / 2 + fm.getAscent() + offsetY;
        
        g2.setColor(Color.BLACK);
        g2.drawString(texto, x + 4, y + 4);
        g2.setColor(color);
        g2.drawString(texto, x, y);
    }

    // Deteccion nativa de teclado fisico (Cuando pulsas una tecla hacia abajo)
    @Override
    public void keyPressed(KeyEvent e) {
        int codigo = e.getKeyCode();
        
        if (carreraTerminada) {
            if (codigo == KeyEvent.VK_ENTER) {
                configurarPartidaConCarros(ultimaRutaMapa, modoDosJugadores, ultimoCarroP1, ultimoCarroP2);
                iniciarJuego();
                return;
            } else if (codigo == KeyEvent.VK_ESCAPE) {
                detenerMusica();
                ventanaPadre.mostrarMenu();
                return; 
            }
        }

        if (codigo == KeyEvent.VK_W) jugador1.acelerando = true;
        if (codigo == KeyEvent.VK_S) jugador1.frenando = true;
        if (codigo == KeyEvent.VK_A) jugador1.girandoIzquierda = true;
        if (codigo == KeyEvent.VK_D) jugador1.girandoDerecha = true;

        if (modoDosJugadores && jugador2 != null && !jugador2.isIA()) {
            if (codigo == KeyEvent.VK_UP) jugador2.acelerando = true;
            if (codigo == KeyEvent.VK_DOWN) jugador2.frenando = true;
            if (codigo == KeyEvent.VK_LEFT) jugador2.girandoIzquierda = true;
            if (codigo == KeyEvent.VK_RIGHT) jugador2.girandoDerecha = true;
        }

        if (codigo == KeyEvent.VK_ESCAPE && !carreraTerminada) {
            detenerJuego();
            ventanaPadre.mostrarMenu();
        }
    }

    // Cuando sueltas la tecla
    @Override
    public void keyReleased(KeyEvent e) {
        int codigo = e.getKeyCode();
        if (codigo == KeyEvent.VK_W) jugador1.acelerando = false;
        if (codigo == KeyEvent.VK_S) jugador1.frenando = false;
        if (codigo == KeyEvent.VK_A) jugador1.girandoIzquierda = false;
        if (codigo == KeyEvent.VK_D) jugador1.girandoDerecha = false;

        if (modoDosJugadores && jugador2 != null && !jugador2.isIA()) {
            if (codigo == KeyEvent.VK_UP) jugador2.acelerando = false;
            if (codigo == KeyEvent.VK_DOWN) jugador2.frenando = false;
            if (codigo == KeyEvent.VK_LEFT) jugador2.girandoIzquierda = false;
            if (codigo == KeyEvent.VK_RIGHT) jugador2.girandoDerecha = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    // Controlador de inputs remotos provenientes del servidor HTTP (Celular via red WiFi)
    public void teclaCelular(String tecla, boolean estado) {
        if (!carreraTerminada) {
            
            // Replicamos el input local para el jugador 1
            if (jugador1 != null) {
                switch(tecla) {
                    case "w": jugador1.acelerando = estado; break;
                    case "s": jugador1.frenando = estado; break;
                    case "a": jugador1.girandoIzquierda = estado; break;
                    case "d": jugador1.girandoDerecha = estado; break;
                }
            }
            
            // Enrutamiento del segundo cliente web
            if (modoDosJugadores && jugador2 != null) {
                switch(tecla) {
                    case "up": jugador2.acelerando = estado; break;
                    case "down": jugador2.frenando = estado; break;
                    case "left": jugador2.girandoIzquierda = estado; break;
                    case "right": jugador2.girandoDerecha = estado; break;
                }
            }
        }
    }
}