package mal.step1_read_print;

public class Main {

    MalType READ(String input) { return Reader.fromString(input).form(); }
    String EVAL(MalType input) { return input.toString(); }
    String PRINT(String input) { return input; }

    void rep() {
        var console = System.console();
        String input = console.readLine("user> ");
        while (input != null) {
            try {
                System.out.println(PRINT(EVAL(READ(input))));
            } catch (Exception e) {
                e.printStackTrace();
            }
            input = console.readLine("user> ");
        }
    }

    private void prompt() {
        System.out.print("user> ");
        System.out.flush();
    }

    public static void main(String[] args) {
        new Main().rep();
    }

}
