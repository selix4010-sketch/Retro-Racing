package com.racing.engine;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

// Modulo de API embebida que despliega una interfaz HTML en la red local y escucha inputs
public class ServidorControl {
    private HttpServer server;
    private PanelJuego panelJuego;

    public ServidorControl(PanelJuego panelJuego) {
        this.panelJuego = panelJuego;
    }

    // Levanta un hilo backend mapeando endpoints en el puerto 8080
    public void iniciar() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // Inyeccion de dependencias via constructor para el renderizado del layout movil
            server.createContext("/", new ControlUIHandler("w", "s", "a", "d", "JUGADOR 1"));
            server.createContext("/p2", new ControlUIHandler("up", "down", "left", "right", "JUGADOR 2"));
            
            // Endpoint que funciona como receptor de los pings Javascript
            server.createContext("/cmd", new ComandoHandler());
            server.setExecutor(null);
            server.start();
            System.out.println(">>> Servidor iniciado. J1: http://IP:8080/ | J2: http://IP:8080/p2");
        } catch (Exception e) {
            System.err.println("No se pudo iniciar el servidor del celular.");
        }
    }

    // Despachador HTTP encargado de renderizar la pagina web en los clientes que se conectan
    class ControlUIHandler implements HttpHandler {
        String tArr, tAba, tIzq, tDer, titulo;

        public ControlUIHandler(String arr, String aba, String izq, String der, String titulo) {
            this.tArr = arr; this.tAba = aba; this.tIzq = izq; this.tDer = der; this.titulo = titulo;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            // Documento HTML puro inyectado con estilos flexbox y touch events para evitar input lag
            String html = "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no'>"
                    + "<style>body{background:#000014;display:flex;justify-content:space-between;align-items:center;height:100vh;margin:0;padding:40px;box-sizing:border-box;font-family:sans-serif;user-select:none;touch-action:none;}"
                    + ".btn{background:#000096;border:4px solid #FFD700;border-radius:15px;display:flex;justify-content:center;align-items:center;font-size:35px;font-weight:bold;color:white;box-shadow: 0 8px #000050;}"
                    + ".btn:active{background:#FFD700;color:black;box-shadow: 0 2px #000050;transform: translateY(6px);}"
                    + ".dpad{display:flex;gap:20px;}.act{display:flex;gap:20px;align-items:flex-end;}"
                    + ".tit{position:absolute;top:10px;left:0;width:100%;text-align:center;color:#FFD700;font-size:22px;font-weight:bold;}</style></head>"
                    + "<body><div class='tit'>" + titulo + "</div>"
                    + "<div class='dpad'>"
                    + "<div class='btn' style='width:90px;height:90px;' ontouchstart='s(\""+tIzq+"\",1)' ontouchend='s(\""+tIzq+"\",0)'>&lt;</div>"
                    + "<div class='btn' style='width:90px;height:90px;' ontouchstart='s(\""+tDer+"\",1)' ontouchend='s(\""+tDer+"\",0)'>&gt;</div>"
                    + "</div><div class='act'>"
                    + "<div class='btn' style='width:90px;height:90px;' ontouchstart='s(\""+tAba+"\",1)' ontouchend='s(\""+tAba+"\",0)'>F</div>"
                    + "<div class='btn' style='width:110px;height:110px;font-size:45px;' ontouchstart='s(\""+tArr+"\",1)' ontouchend='s(\""+tArr+"\",0)'>A</div>"
                    + "</div>"
                    + "<script>"
                    + "document.addEventListener('contextmenu', event => event.preventDefault());"
                    // Realiza peticiones asincronas rapidas al presionar y soltar cada boton del telefono
                    + "function s(k,v){fetch('/cmd?k='+k+'&v='+v);}"
                    + "</script></body></html>";
            t.sendResponseHeaders(200, html.length());
            OutputStream os = t.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }

    // Endpoint de procesamiento de logica. Extrae variables GET y se las pasa al Game Loop en Java
    class ComandoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            if (query != null && query.contains("=")) {
                String[] params = query.split("&");
                String k = params[0].split("=")[1]; // Identificador de la tecla (w,a,s,d,up,etc)
                String v = params[1].split("=")[1]; // Estado booleano: 1 apretado, 0 suelto
                panelJuego.teclaCelular(k, v.equals("1"));
            }
            String res = "ok";
            t.sendResponseHeaders(200, res.length());
            OutputStream os = t.getResponseBody();
            os.write(res.getBytes());
            os.close();
        }
    }
}