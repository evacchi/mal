package mal.step2_eval;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vavr.collection.List;

import static io.vavr.API.*;
import static io.vavr.Predicates.*;

import static java.util.stream.Collectors.joining;

enum Token {
    WhiteSpace("[\\s,]+", true),
    Comment(";.*", true),
    String("\"(?:\\\\.|[^\\\\\"])+\"", false),
    Atom("[^\\s\\[\\]{}('\"`,;)]+", false),
    LeftParen("\\(", false),
    RightParen("\\)", false);
    final Pattern pattern;
    final boolean ignored;

    Token(String pattern, boolean ignored) {
        this.ignored = ignored;
        this.pattern = Pattern.compile("^"+pattern);
    }
    QualifiedToken match(String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find()?
                new QualifiedToken(this, matcher.group()) :
                null;
    }
}

class QualifiedToken {
    final Token token;
    final String value;

    public QualifiedToken(Token token, String value) {
        this.token = token;
        this.value = value;
    }
}

public class Reader {

    private final List<QualifiedToken> tokens;
    private int position;

    public static Reader fromString(String input) {
        var tokens = tokenize(input);
        return new Reader(tokens);
    }

    public MalType form() {
        QualifiedToken tok = this.peek();
        if (tok.token == Token.LeftParen) {
            return list();
        } else {
            return atom();
        }
    }

    static List<QualifiedToken> tokenize(String input) {
        var tokens = List.<QualifiedToken>empty();
        while (!input.isEmpty()) {
            String current = input;
            QualifiedToken matched = Arrays.stream(Token.values())
                    .map(t -> t.match(current))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .get();
            input = input.substring(matched.value.length());
            if (matched.token.ignored) continue;
            tokens = tokens.append(matched);
        }
        return tokens;
    }

    public Reader(List<QualifiedToken> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }

    private QualifiedToken next() {
        return tokens.get(position++);
    }

    private QualifiedToken peek() {
        return tokens.get(position);
    }


    private MalType atom() {
        var token = next();
        try {
            return new Number(Integer.parseInt(token.value));
        } catch (NumberFormatException e) {
            return new Symbol(token.value);
        }
    }

    private MalType list() {
        var types = List.<MalType>empty();
        this.next(); // discard LeftParen
        while (this.peek().token != Token.RightParen) {
            types = types.append(form());
        }
        next(); // discard RightParen
        return new MalList(types);
    }

    private MalType form(Reader r) {
        QualifiedToken tok = r.peek();
        if (tok.token == Token.LeftParen) {
            return list();
        } else {
            return atom();
        }
    }
}

abstract class MalType {
    public MalType apply() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}

abstract class Atom extends MalType {}

final class Number extends Atom {
    final long value;
    public Number(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}

final class Symbol extends Atom {
    final String value;
    public Symbol(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Symbol && value.equals(((Symbol)obj).value);
    }
}

final class MalList extends MalType {
    final List<MalType> value;
    public MalList(List<MalType> value) {
        this.value = value;
    }
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value
                .map(MalType::toString)
                .collect(joining(" ", "(", ")"));
    }
    public MalType apply() {
        return Match(value.head()).of(
                Case($(instanceOf(MalFunction.class)), f -> f.apply(value.tail())));
    }
}

final class MalFunction extends MalType {
    private final Function<List<MalType>, MalType> value;

    public MalFunction(Function<List<MalType>, MalType> value) {
        this.value = value;
    }

    public MalType apply(List<MalType> args) {
        return value.apply(args);
    }
}



