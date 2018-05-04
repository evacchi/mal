package mal.step2_eval;

import java.util.NoSuchElementException;

import io.vavr.API;
import io.vavr.collection.Map;

public class Env {
    private Map<MalType, MalType> symbolTable;

    public Env(Map<MalType, MalType> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public Env() {
        this(API.Map());
    }

    public MalType lookup(MalType s) {
        return symbolTable.get(s).getOrElseThrow(() -> { throw new NoSuchElementException(s.toString()); });
    }
}