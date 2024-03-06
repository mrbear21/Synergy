package me.synergy.modules;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import me.synergy.brains.Spigot;

public class WebServer {

	  private Spigot spigot;
	  
	  public WebServer(Spigot spigot) {
		  this.spigot = spigot;
	  }

    public void start() {
    	if (spigot.getConfig().getBoolean("web-server")) {
            try {
                int port = spigot.getConfig().getInt("votifier.port");
                spigot.WEBSERVER = HttpServer.create(new InetSocketAddress(port), 0);
                spigot.getWeb().createContext("/", new VoteHandler());
                spigot.getWeb().start();
                spigot.getLogger().info("HTTP server started on port "+port);
            } catch (IOException e) {
                spigot.getLogger().severe("Failed to start HTTP server: " + e.getMessage());
            }
	        spigot.getLogger().info(this.getClass().getSimpleName()+" module has been initialized!");
    	}
    }

    public void stop() {

        if (spigot.getWeb() != null) {
        	spigot.getWeb().stop(0);
            spigot.getLogger().info("HTTP server stopped");
        }
    }

    private class VoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Отримання інформації про голосування з запиту
            String query = exchange.getRequestURI().getQuery();

            // Перевірка, чи є параметр голосування
            if (query != null) {
                // Розбиття параметрів на окремі пари ключ-значення
                String[] params = query.split("&");
                for (String param : params) {
                    // Розбиття пари ключ-значення на ключ та значення
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];
                        // Опрацювання кожного параметра голосування
                        spigot.getLogger().info("Received vote parameter: " + key + "=" + value);
                        // Додаткова логіка для обробки параметрів голосування
                    }
                }
            }

            // Відправлення відповіді клієнту
            String response = "Received vote!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
