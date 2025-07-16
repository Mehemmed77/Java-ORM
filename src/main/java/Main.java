
import migrationManager.ManagerHandler;


public class Main {
    public static void main(String[] args) {
        ManagerHandler managerHandler = new ManagerHandler();
        managerHandler.setup();
//        if (args.length == 0) {
//            System.out.println("Usage: java Main <command>");
//            return;
//        }
//
//        switch (args[0]) {
//            case "makemigrations":
//                ManagerHandler managerHandler = new ManagerHandler();
//                managerHandler.setup();
//                break;
//            default:
//                System.out.println("Unknown command: " + args[0]);
//                break;
//        }
    }
}
