package by.pavel;

import by.pavel.parser.OBJParser;
import by.pavel.scene.MainWindow;

public class App {

    public static int WIDTH = 800;
    public static int HEIGHT = 600;

    public static void main(String[] args) {
        OBJParser objParser = new OBJParser();
//        OBJData objData = objParser.parseFile("src/main/resources/suzanne.obj");

        MainWindow t = new MainWindow(WIDTH, HEIGHT);
        t.start();
    }
}
