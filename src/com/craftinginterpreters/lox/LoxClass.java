package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguements) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initialiser = findMethod("init");

        if (initialiser != null) {
            initialiser.bind(instance).call(interpreter, arguements);
        }

        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initialiser = findMethod("init");
        if (initialiser == null) {
            return 0;
        }
        return initialiser.arity();
    }

}
