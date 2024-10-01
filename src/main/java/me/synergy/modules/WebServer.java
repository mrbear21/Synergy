package me.synergy.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.sun.net.httpserver.HttpServer;

import me.synergy.brains.Synergy;

public class WebServer{

    private static HttpServer server;
    private static int port = Synergy.getConfig().getInt("web-server.port");
    private static String serverAddress = Synergy.getConfig().getString("web-server.domain");
    private static String fullAddress = "http://" + serverAddress + ":"+port;

    public void initialize() {
    	if (!Synergy.getConfig().getBoolean("web-server.enabled")) {
    		return;
    	}

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new TexturePackHandler());
            server.setExecutor(null);
            server.start();
            Synergy.getLogger().info("Web server started on port "+port);
        } catch (IOException e) {
        	Synergy.getLogger().warning("Failed to start web server: " + e.getMessage());
        }
        loadWebFiles();
        if (Synergy.isRunningSpigot()) {
        	loadResourcePackFolder();
        }
    }

    private void loadWebFiles() {
    	File dataFolder = new File("plugins/Synergy");
        File webFolder = new File(dataFolder, "web");
        if (!webFolder.exists()) {
            boolean created = webFolder.mkdirs();
            if (!created) {
            	Synergy.getLogger().warning("Failed to create the 'web' folder!");
                return;
            }
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("index.html")) {
                File indexFile = new File(webFolder, "index.html");
                Files.copy(inputStream, indexFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Synergy.getLogger().info("Copied index.html to the 'web' folder.");
            } catch (IOException e) {
            	Synergy.getLogger().warning("Failed to copy index.html to the 'web' folder!");
                e.printStackTrace();
            }
        }
    }

    private void loadResourcePackFolder() {
        File webFolder = new File("resourcepack");
        if (!webFolder.exists()) {
            boolean created = webFolder.mkdirs();
            if (!created) {
            	Synergy.getLogger().warning("Failed to create the 'web' folder!");
                return;
            }
        }
    }

    public static String getFullAddress() {
    	return fullAddress;
    }

    public void shutdown() {
        if (server != null) {
            server.stop(0);
            Synergy.getLogger().info("Web server server stopped");
        }
    }

	private class TexturePackHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/resourcepack")) {
                File texturePackFile = new File(path.substring(1));
                if (texturePackFile.exists()) {
                    exchange.sendResponseHeaders(200, texturePackFile.length());
                    OutputStream os = exchange.getResponseBody();
                    FileInputStream fis = new FileInputStream(texturePackFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                    os.close();
                } else {
                    String response = "Texture pack not found";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else if ("/".equals(path)) {
                File webFolder = new File("plugins/Synergy/web");
                File indexFile = new File(webFolder, "index.html");
                if (indexFile.exists()) {
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream os = exchange.getResponseBody();
                    Files.copy(indexFile.toPath(), os);
                    os.close();
	            } else {
	                exchange.sendResponseHeaders(404, 0);
	                exchange.close();
	            }
	        } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }



}
