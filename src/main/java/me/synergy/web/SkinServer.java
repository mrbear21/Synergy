package me.synergy.web;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import me.synergy.integrations.MojangAPI;
import me.synergy.integrations.SkinRestorerAPI;

public class SkinServer {

    private static final long CACHE_TIMEOUT = 10 * 60 * 1000;
    private static final Map<String, CacheEntry> imageCache = new HashMap<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    static class CacheEntry {
        BufferedImage image;
        long timestamp;
        Format format;

        CacheEntry(BufferedImage image, long timestamp, Format format) {
            this.image = image;
            this.timestamp = timestamp;
            this.format = format;
        }

        enum Format {
            HEAD, SKIN
        }
    }

    static abstract class AbstractHandler implements HttpHandler {
        protected abstract CacheEntry.Format getFormat();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, 0);

            String[] segments = exchange.getRequestURI().getPath().split("/");

            if (segments.length >= 3 && !segments[2].isEmpty()) {
                UUID uuid = segments[2].length() == 36 ? UUID.fromString(segments[2]) : null;
                String name = segments[2].length() != 36 ? segments[2] : null;
                String cacheKey = (uuid != null) ? uuid.toString() : name;

                executorService.submit(() -> {
                    try {
                        CacheEntry cacheEntry = getCachedImage(cacheKey, getFormat());
                        if (cacheEntry == null) {
                            String skinUrl = getSkinUrl(uuid, name);
                            try (InputStream skin = (skinUrl != null) ? getSkinStream(skinUrl) : getDefaultSkin()) {
                                BufferedImage processedImage = processImage(ImageIO.read(skin != null ? skin : getDefaultSkin()));
                                cacheImage(cacheKey, processedImage, getFormat());
                                sendImageResponse(exchange, processedImage);
                            }
                        } else {
                            sendImageResponse(exchange, cacheEntry.image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        exchange.close();
                    }
                });
            }
        }
        
        private void sendImageResponse(HttpExchange exchange, BufferedImage image) throws IOException {
            try (OutputStream os = exchange.getResponseBody()) {
                ImageIO.write(image, "png", os);
            }
        }

        protected BufferedImage processImage(BufferedImage image) {
            return image;
        }

        protected CacheEntry getCachedImage(String cacheKey, CacheEntry.Format format) {
            CacheEntry cacheEntry = imageCache.get(cacheKey);
            if (cacheEntry != null && cacheEntry.format == format &&
                (System.currentTimeMillis() - cacheEntry.timestamp) < CACHE_TIMEOUT) {
                return cacheEntry;
            } else {
                imageCache.remove(cacheKey);
                return null;
            }
        }

        protected void cacheImage(String cacheKey, BufferedImage image, CacheEntry.Format format) {
            if (cacheKey != null) {
                imageCache.put(cacheKey, new CacheEntry(image, System.currentTimeMillis(), format));
            }
        }

        protected InputStream getDefaultSkin() {
            return getClass().getResourceAsStream("/steve.png");
        }
    }

    static class SkinHandler extends AbstractHandler {
        @Override
        protected CacheEntry.Format getFormat() {
            return CacheEntry.Format.SKIN;
        }
    }

    static class HeadHandler extends AbstractHandler {
        @Override
        protected CacheEntry.Format getFormat() {
            return CacheEntry.Format.HEAD;
        }

        @Override
        protected BufferedImage processImage(BufferedImage image) {
            BufferedImage combined = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = combined.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(image.getSubimage(8, 8, 8, 8), 0, 0, 128, 128, null);
            g2d.drawImage(image.getSubimage(40, 8, 8, 8), 0, 0, 128, 128, null);
            g2d.dispose();
            return combined;
        }
    }

    private static String getSkinUrl(UUID uuid, String name) {
        String skin = SkinRestorerAPI.getSkinTextureURL(uuid, name);
        if (skin != null) {
            return skin;
        }
        return MojangAPI.getSkinTextureURL(uuid, name);
    }

    private static InputStream getSkinStream(String skinUrl) throws IOException {
        @SuppressWarnings("deprecation")
		HttpURLConnection connection = (HttpURLConnection) new URL(skinUrl).openConnection();
        connection.setRequestMethod("GET");
        return connection.getResponseCode() == 200 ? connection.getInputStream() : null;
    }
}

