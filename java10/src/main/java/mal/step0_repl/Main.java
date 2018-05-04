package mal.step0_repl;

public class Main {

    String READ(String input) { return input; }
    String EVAL(String input) { return input; }
    String PRINT(String input) { return input; }

    void rep() {
        var console = System.console();
        String input = console.readLine("user> ");
        while (input != null) {
            System.out.println(PRINT(EVAL(READ(input))));
            input = console.readLine("user> ");
        }
    }

    public static void main(String[] args) {
        new Main().rep();
    }

}
