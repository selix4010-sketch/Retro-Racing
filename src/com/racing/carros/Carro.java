package com.racing.carros;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Carro {
    
    private double x, y;
    private final double xInicial, yInicial, anguloInicial;
    private double velocidad;
    private double angulo; 
    
    public boolean acelerando, frenando, girandoIzquierda, girandoDerecha;

    private final double ACELERACION = 0.18; 
    private final double FRICCION = 0.05;
    private double velocidadMaximaActual = 5.0; 
    private final double VELOCIDAD_ROTACION = 4.0;

    private BufferedImage sprite;
    private boolean esIA;
    private Color colorEmergencia;
    
    private String nombrePiloto;
    private int vueltas = 0;
    private boolean pasoCheckpoint = false;
    private long tiempoUltimoPasoMeta = 0;

    private int[][] waypoints;
    private int waypointActual = 0;
    
    private int[] contadorChoquesPorNodo;
    private double[] offsetAprendizajeX;

    public Carro(double xStart, double yStart, double anguloStart, String rutaSprite, boolean esIA, Color colorFallback, String nombre) {
        this.x = xStart;
        this.y = yStart;
        this.xInicial = xStart;
        this.yInicial = yStart;
        this.angulo = anguloStart;
        this.anguloInicial = anguloStart;
        this.esIA = esIA;
        this.colorEmergencia = colorFallback;
        this.nombrePiloto = nombre;
        this.velocidad = 0;
        
        cargarSprite(rutaSprite);
        
        waypoints = new int[][] {
            {103, 346}, {108, 405}, {116, 448}, {161, 494}, {213, 547},
            {272, 605}, {337, 644}, {385, 624}, {396, 563}, {402, 493},
            {438, 442}, {488, 436}, {537, 457}, {554, 502}, {562, 555},
            {578, 623}, {626, 639}, {670, 628}, {678, 586}, {678, 529},
            {676, 461}, {674, 395}, {658, 357}, {604, 342}, {522, 342},
            {459, 340}, {403, 323}, {397, 290}, {426, 264}, {476, 258},
            {539, 261}, {622, 261}, {662, 246}, {675, 197}, {673, 153},
            {664, 116}, {610, 106}, {546, 106}, {437, 108}, {338, 111},
            {298, 130}, {292, 178}, {287, 235}, {289, 302}, {280, 354},
            {267, 376}, {245, 378}, {215, 373}, {203, 341}, {203, 308},
            {198, 255}, {201, 176}, {197, 133}, {159, 112}, {114, 120},
            {106, 156}, {104, 212}, {104, 252}, {104, 301}, {104, 321},
            {101, 364}
        };

        contadorChoquesPorNodo = new int[waypoints.length];
        offsetAprendizajeX = new double[waypoints.length];
    }

    private void cargarSprite(String ruta) {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(ruta));
        } catch (Exception e) {
            sprite = null;
        }
    }

    private boolean esMuro(double testX, double testY, BufferedImage mapa, int anchoPantalla, int altoPantalla) {
        if (mapa == null || anchoPantalla <= 0 || altoPantalla <= 0) return true;
        
        int mapX = (int) (testX * ((double) mapa.getWidth() / anchoPantalla));
        int mapY = (int) (testY * ((double) mapa.getHeight() / altoPantalla));
        
        if (mapX >= 0 && mapX < mapa.getWidth() && mapY >= 0 && mapY < mapa.getHeight()) {
            int rgb = mapa.getRGB(mapX, mapY);
            int alpha = (rgb >> 24) & 0xff;
            Color colorPixel = new Color(rgb, true);
            
            boolean esBlanco = colorPixel.getRed() > 220 && colorPixel.getGreen() > 220 && colorPixel.getBlue() > 220;
            boolean esRojo = colorPixel.getRed() > 150 && colorPixel.getGreen() < 100 && colorPixel.getBlue() < 100;
            
            return (alpha == 0 || esBlanco || esRojo);
        }
        return true;
    }

    private int obtenerWaypointMasCercano() {
        double menorDist = Double.MAX_VALUE;
        int idx = 0;
        for(int i = 0; i < waypoints.length; i++) {
            double d = Math.sqrt(Math.pow(waypoints[i][0] - x, 2) + Math.pow(waypoints[i][1] - y, 2));
            if(d < menorDist) {
                menorDist = d;
                idx = i;
            }
        }
        return idx;
    }

    public boolean vaEnSentidoContrario() {
        if (velocidad <= 0.5) return false; 
        int closest = obtenerWaypointMasCercano();
        int siguienteIdx = (closest + 3) % waypoints.length; 
        double destX = waypoints[siguienteIdx][0];
        double destY = waypoints[siguienteIdx][1];
        
        double anguloDeseado = Math.toDegrees(Math.atan2(destY - y, destX - x));
        double diff = Math.abs(angulo - anguloDeseado);
        while (diff > 180) diff = Math.abs(diff - 360);
        
        return diff > 130; 
    }

    public void actualizar(BufferedImage mapa, Rectangle lineaMeta, Rectangle checkpoint, boolean motorEncendido, int anchoPantalla, int altoPantalla) {
        if (!motorEncendido) return; 

        if (esIA) procesarInteligenciaArtificial();

        if (acelerando) velocidad += ACELERACION;
        else if (frenando) velocidad -= ACELERACION;

        if (velocidad > 0) {
            velocidad -= FRICCION;
            if (velocidad < 0) velocidad = 0;
        } else if (velocidad < 0) {
            velocidad += FRICCION;
            if (velocidad > 0) velocidad = 0;
        }

        if (velocidad > velocidadMaximaActual) velocidad = velocidadMaximaActual;
        if (velocidad < -velocidadMaximaActual / 2) velocidad = -velocidadMaximaActual / 2;

        if (Math.abs(velocidad) > 0.1) {
            double dirRot = (velocidad > 0) ? 1 : -1;
            if (girandoIzquierda) angulo -= VELOCIDAD_ROTACION * dirRot;
            if (girandoDerecha) angulo += VELOCIDAD_ROTACION * dirRot;
        }

        if (esMuro(x, y, mapa, anchoPantalla, altoPantalla)) {
            int closest = obtenerWaypointMasCercano();
            
            if (esIA) {
                contadorChoquesPorNodo[closest]++;
                offsetAprendizajeX[closest] += (Math.random() * 4.0 - 2.0);
            }

            double destX = waypoints[closest][0];
            double destY = waypoints[closest][1];
            double dx = destX - x;
            double dy = destY - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist > 0) {
                x += (dx / dist) * 2.5;
                y += (dy / dist) * 2.5;
            }
            velocidad *= 0.7;
            if (esIA) waypointActual = closest;
            
        } else {
            double futuroX = x + Math.cos(Math.toRadians(angulo)) * velocidad;
            double futuroY = y + Math.sin(Math.toRadians(angulo)) * velocidad;

            if (esMuro(futuroX, futuroY, mapa, anchoPantalla, altoPantalla)) {
                velocidad *= 0.85;
                if (esIA) {
                    int closest = obtenerWaypointMasCercano();
                    contadorChoquesPorNodo[closest]++;
                    waypointActual = closest;
                }

                boolean puedeMoverX = !esMuro(futuroX, y, mapa, anchoPantalla, altoPantalla);
                boolean puedeMoverY = !esMuro(x, futuroY, mapa, anchoPantalla, altoPantalla);

                if (puedeMoverX && !puedeMoverY) {
                    x = futuroX;
                } else if (puedeMoverY && !puedeMoverX) {
                    y = futuroY;
                } else {
                    velocidad *= 0.5;
                }
            } else {
                x = futuroX;
                y = futuroY;
            }
        }

        long tiempoActual = System.currentTimeMillis();

        if (checkpoint.contains(x, y)) {
            pasoCheckpoint = true;
        }
        
        if (lineaMeta.contains(x, y) && pasoCheckpoint) {
            if (tiempoActual - tiempoUltimoPasoMeta > 3000) {
                vueltas++;
                pasoCheckpoint = false; 
                tiempoUltimoPasoMeta = tiempoActual;
            }
        }
    }

    private void procesarInteligenciaArtificial() {
        double destX = waypoints[waypointActual][0] + offsetAprendizajeX[waypointActual];
        double destY = waypoints[waypointActual][1];
        
        if (contadorChoquesPorNodo[waypointActual] > 2) {
            velocidadMaximaActual = 3.5; 
        } else {
            velocidadMaximaActual = 5.0; 
        }

        double anguloDeseado = Math.toDegrees(Math.atan2(destY - y, destX - x));
        double diferenciaAngulo = anguloDeseado - angulo;
        
        while (diferenciaAngulo <= -180) diferenciaAngulo += 360;
        while (diferenciaAngulo > 180) diferenciaAngulo -= 360;

        girandoDerecha = diferenciaAngulo > 5;
        girandoIzquierda = diferenciaAngulo < -5;
        
        if (Math.abs(velocidad) < 2.0) {
            if (girandoDerecha) angulo += 2.0;
            if (girandoIzquierda) angulo -= 2.0;
        }

        acelerando = Math.abs(diferenciaAngulo) < 90;

        double dist = Math.sqrt(Math.pow(destX - x, 2) + Math.pow(destY - y, 2));
        if (dist < 70) {
            waypointActual++;
            if (waypointActual >= waypoints.length) waypointActual = 0;
        }
    }

    public void dibujar(Graphics2D g2) {
        AffineTransform txOriginal = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(angulo));
        
        if (sprite != null) {
            g2.rotate(Math.toRadians(90));
            g2.drawImage(sprite, -9, -15, 18, 30, null); 
        } else {
            g2.setColor(colorEmergencia);
            g2.fillRect(-15, -9, 30, 18); 
            g2.setColor(Color.WHITE);
            g2.fillRect(5, -3, 8, 6); 
        }
        g2.setTransform(txOriginal);
    }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIA() { return esIA; }
    public int getVueltas() { return vueltas; }
    public String getNombre() { return nombrePiloto; }
}