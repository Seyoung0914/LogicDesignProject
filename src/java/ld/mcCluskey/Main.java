package mcCluskey;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        McCluskeyImpl mcCluskeyImpl = new McCluskeyImpl(sc);
        mcCluskeyImpl.solve();
    }
}
