package arep.taller1.areptaller1;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

/**
 * Client class that tests HttpServer functionalities
 */
public class Cliente {

    public static void main(String[] args) throws IOException {
        System.out.print("Escribe la película que quieras buscar ");
        Scanner obj = new Scanner(System.in);
        String input = obj.nextLine();
        while (!input.equals("")) {
            JSONObject json = new JSONObject(ArepTaller1.getMovieData(input));
            if (json.get("Response").equals("False")) {
                System.out.println("Movie not found");
            } else {
                System.out.println("Server: " + json);
            }
            System.out.print("Escribe la película que quieras buscar: ");
            input = obj.nextLine();
        }
    }
}
