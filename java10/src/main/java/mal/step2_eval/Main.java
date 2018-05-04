package mal.step2_eval;

import static io.vavr.API.*;
import static io.vavr.Predicates.*;
import static java.util.function.Function.*;

public class Main {

    MalType READ(String input) { return Reader.fromString(input).form(); }
    MalType EVAL(MalType input, Env env) {
        return Match(input).of(
                Case($(instanceOf(MalList.class).and(MalList::isEmpty)), identity()),
                Case($(instanceOf(MalList.class)), l -> evalAst(l, env).apply()),
                Case($(), any -> evalAst(any, env)));
    }


    public MalType evalAst(MalType type, Env env) {
        return Match(type).of(
                Case($(instanceOf(Symbol.class)), env::lookup),
                Case($(instanceOf(MalList.class)), l ->
                        new MalList(l.value.map(v -> EVAL(v, env)))),
                Case($(instanceOf(MalFunction.class)), f -> { throw new UnsupportedOperationException("not yet"); }),
                Case($(), identity())
        );
    }

    String PRINT(MalType input) { return input.toString(); }

    Env env = new Env(Map(
            new Symbol("+"),
            new MalFunction(l -> new Number(((Number) l.head()).value + ((Number) l.tail().head()).value)),

            new Symbol("-"),
            new MalFunction(l -> new Number(((Number) l.head()).value - ((Number) l.tail().head()).value)),

            new Symbol("*"),
            new MalFunction(l -> new Number(((Number) l.head()).value * ((Number) l.tail().head()).value)),

            new Symbol("/"),
            new MalFunction(l -> new Number(((Number) l.head()).value / ((Number) l.tail().head()).value))
    ));

    void rep() {
        var console = System.console();
        String input = console.readLine("user> ");
        while (input != null) {
            try {
                System.out.println(PRINT(EVAL(READ(input), env)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            input = console.readLine("user> ");
        }
    }

    public static void main(String[] args) {
        new Main().rep();
    }

}
