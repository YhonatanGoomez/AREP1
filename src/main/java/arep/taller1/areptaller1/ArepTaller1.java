
package arep.taller1.areptaller1;

import com.google.gson.JsonObject;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import org.json.JSONObject;
import com.google.gson.JsonParser;

public class ArepTaller1 {

    private static final String USER_AGENT = "Mozilla/5.0 Chrome/51.0.2704.103 Safari/537.36";
    private static final String API_KEY = "c2d09dcc";
    private static final String GET_URL = "http://www.omdbapi.com/?t=%1$s&apikey=%2$s";
    private static final HashMap<String, String> cache = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("No se puede escuchar el puerto 35000");
            System.exit(1);
        }

        boolean running = true;
        Socket clientSocket = null;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean firstLine = true;
            String uriString = "";
            while ((inputLine = in.readLine()) != null) {
                if (firstLine) {
                    uriString = inputLine.split(" ")[1];
                    firstLine = false;
                }
                // System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (uriString.split("=").length > 1) {
                outputLine = changeHTML(uriString.split("=")[1]);
            } else if (uriString.startsWith("/movie")) {
                outputLine = "";

            } else {
                outputLine = getIndexResponse();
            }
            out.println(outputLine);
            out.close();
            in.close();

        }
        clientSocket.close();
        serverSocket.close();
    }

    /**
     * Method that finds the movie the user is looking for
     *
     * @param movieName movie's name
     * @return String information of the movie in a Json format
     */
    public static String getMovieData(String movieName) throws IOException {
        String movieJson = "";
        if (cache.containsKey(movieName)) {
            movieJson = cache.get(movieName);
            System.out.println("Pelicula en cache");
        } else {
            String url = String.format(GET_URL, movieName, API_KEY);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            //The following invocation perform the connection implicitly before getting the code
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString());
                return response.toString();
            } else {
                System.out.println("GET request not worked");
            }
            System.out.println("GET DONE");
            cache.put(movieName, movieJson);
            System.out.println("Guardada en cache");
        }
        return movieJson;
    }

    /**
     * Method that displays information of the movie the user typed
     *
     * @param movie movie name
     * @return String according to the movie
     * @throws IOException
     */
    public static String changeHTML(String movie) throws IOException {
        if (cache.containsKey(movie)) {
            return "HTTP/1.1 200 OK"
                    + "Content-Type: text/html\r\n"
                    + "\r\n" + cache.get(movie);
        } else {
            String movieData = getMovieData(movie);
            cache.put(movie, formatMovieData(movieData));
            return "HTTP/1.1 200 OK"
                    + "Content-Type: text/html\r\n"
                    + "\r\n" + formatMovieData(movieData);
        }

        
    }



    /**
     * Method that converts a json into a html table
     *
     * @param movieJson data of the movie in a Json format
     * @return html table
     */
    public static String htmlTable(String movieJson) throws JSONException {
        String table = "<table>";
        JSONObject jsonObject = new JSONObject(movieJson);
        for (String key : jsonObject.keySet()) {
            table += "<tr> \n <td>" + key + "</td> \n <td>" + jsonObject.get(key).toString() + "</td> </tr> \n";
        }
        table += "</table>";
        return table;
    }
    
        private static String formatMovieData(String movieData) {
        JsonObject jsonObject = JsonParser.parseString(movieData).getAsJsonObject();

        String title = jsonObject.get("Title").getAsString();
        String year = jsonObject.get("Year").getAsString();
        String director = jsonObject.get("Director").getAsString();
        String actors = jsonObject.get("Actors").getAsString();
        String plot = jsonObject.get("Plot").getAsString();
        String posterUrl = jsonObject.get("Poster").getAsString();

        return "<h2>Movie information</h2>"
                + "<p><strong>Title:</strong> " + title + "</p>"
                + "<p><strong>Year:</strong> " + year + "</p>"
                + "<p><strong>Director:</strong> " + director + "</p>"
                + "<p><strong>Actors:</strong> " + actors + "</p>"
                + "<p><strong>Plot:</strong> " + plot+ "</p>"
                + "<img src=\"" + posterUrl + "\" alt=\"Póster de la película\">";
    }

    /**
     * Method that displays a get form
     *
     * @return String
     */
    public static String getIndexResponse() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Movie Form</title>\n"
                + "        <meta charset=\"UTF-8\">\n"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Información de Películas</h1>\n"
                + "        <form action=\"/movie\">\n"
                + "            <label for=\"name\">Nombre de la película:</label><br>\n"
                + "            <input type=\"text\" id=\"name\" name=\"name\" value=\"Escribe el nombre de la película\"><br><br>\n"
                + "            <input type=\"button\" value=\"Enviar\" onclick=\"loadGetMsg()\">\n"
                + "        </form> \n"
                + "        <div id=\"getrespmsg\"></div>\n"
                + "\n"
                + "        <script>\n"
                + "            function loadGetMsg() {\n"
                + "                let nameVar = document.getElementById(\"name\").value;\n"
                + "                const xhttp = new XMLHttpRequest();\n"
                + "                xhttp.onload = function() {\n"
                + "                    document.getElementById(\"getrespmsg\").innerHTML =\n"
                + "                    this.responseText;\n"
                + "                }\n"
                + "                xhttp.open(\"GET\", \"/movie?name=\"+nameVar);\n"
                + "                xhttp.send();\n"
                + "            }\n"
                + "        </script>\n"
                + "\n"
                + "    </body>\n"
                + "</html>";
    }

    /**
     * Method that returns the cache
     *
     * @return cache
     */
    public static HashMap<String, String> getCache() {
        return cache;
    }
}
