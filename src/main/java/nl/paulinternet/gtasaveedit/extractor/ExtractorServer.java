package nl.paulinternet.gtasaveedit.extractor;

import com.sun.net.httpserver.HttpServer;
import nl.paulinternet.gtasaveedit.view.menu.extractor.ExtractorMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ExtractorServer extends Thread {

    private static HttpServer server = null;
    private static Path tempDir = null;
    private ExtractorMenu menu;

    public ExtractorServer(ExtractorMenu menu) {
        this.menu = menu;
    }

    @Override
    public void run() {
        if (server == null) {
            try {
                startServer();
            } catch (IOException e) {
                System.err.println("Unable to start savegame extractor server!");
                e.printStackTrace();
            }
        } else {
            System.err.println("Server already running! " + server.getAddress().toString());
        }
    }

    private void startServer() throws IOException {
        System.out.println("Starting server...");
        tempDir = Files.createTempDirectory("gtasaseExtractor");
        server = HttpServer.create(new InetSocketAddress(8181), 0);
        server.createContext("/add", new FormDataHandler(d -> {
            FormDataHandler.FileData[] fileData = (FormDataHandler.FileData[]) d.toArray();
            for (int i = 0; i < fileData.length; i++) {
                FormDataHandler.FileData f = fileData[i];
                if (f.contentType.equals("application/octet-stream")) {
                    File savegameFile = new File(tempDir.toFile().getAbsolutePath() + File.separator + f.fileName);
                    try (FileOutputStream stream = new FileOutputStream(savegameFile)) {
                        System.out.println("Writing file: '" + savegameFile.getAbsolutePath() + "'");
                        stream.write(f.data);
                        ExtractedSavegameHolder.addSavegame(ExtractorMenu.EXTRACTED_SAVEGAMES_START_IDX + i, new ExtractedSavegameFile(savegameFile, f.fileName), menu);
                    } catch (IOException e) {
                        System.err.println("Unable to write temp file!");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Unknown part: {name: '" + f.name + "', value: '" + Arrays.toString(f.data) + "'}");
                }
            }
        }));
        server.createContext("/upload", httpExchange -> {
            String response = "<html><head><title>GTASASE Uploader</title></head><body><form action=\"/add\" " +
                    "method=\"POST\"enctype=\"multipart/form-data\">Select savegame to add:<input type=\"file\" " +
                    "name=\"savegame\" id=\"savegame\"><input type=\"submit\" value=\"Upload\" name=\"submit\"> " +
                    "</form></body></html>";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.createContext("/list", httpExchange -> {
            StringBuilder builder = new StringBuilder("[");
            ExtractedSavegameHolder.getSaveGameFiles().forEach((i, f) -> {
                String saveGameUrl = server.getAddress().toString() + "/get/" + f.getFileName();
                builder.append("{\"id\": \"").append(i)
                        .append("\", \"name\": \"").append(f.fileName)
                        .append("\", \"uri\": \"").append(saveGameUrl)
                        .append("\"},");
            });
            builder.append("]");
            String response = builder.toString().replaceAll(",]", "");
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.createContext("/version", httpExchange -> {
            String response = "1";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
    }

    public void stopServer() {
        if (server != null) {
            System.out.println("Stopping server...");
            server.stop(0);
            server = null;
            tempDir = null;
        } else {
            System.out.println("Server already stopped!");
        }
    }

    public static class ExtractedSavegameFile {
        private final File saveGame;
        private final String fileName;

        public ExtractedSavegameFile(File saveGame, String fileName) {
            this.saveGame = saveGame;
            this.fileName = fileName;
        }

        public File getSaveGame() {
            return saveGame;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
