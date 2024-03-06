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
            String response = "Hello World";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
