import ui.InputReader;
import ui.ParcelMenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;


public class Main {

    public static void main(String[] args) {
       
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            ParcelMenu menu = new ParcelMenu(new InputReader(reader));
            menu.run();
        } catch (UncheckedIOException e) {
          
            System.out.println();
            System.out.println("Input ended. Closing the Parcel Delivery System.");
        } catch (IOException e) {
           
            System.out.println("Could not close the input: " + e.getMessage());
        }
    }
}
