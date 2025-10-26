package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.values;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Environment {
    public static final Object UNINTIALISED = new Object();

    final Environment enclosing;
    // comeback here..
    // we need to figure how to handle global variables.
    // Currently all environments have a global HM & a local array
    // this is redundant since locals will not use the global HM and vice versa
    // we also need to figure out how to handle the define method
    // At the moment it is using the old search HM structure
    // and not the new indexed pattern for arrays (local vars)
    // This is fine for globals but for locals it wont work
    // since they are not stored in a HM anymore
    private final Map<String, Object> globalValues = new HashMap<>();
    private final List<Object> values = new ArrayList<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (globalValues.containsKey(name.lexeme)) {
            Object value = globalValues.get(name.lexeme);
            if (value == UNINTIALISED) {
                throw new RuntimeError(name, "Uninitialised variable '" + name.lexeme + "'.");
            }
            return value;
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (globalValues.containsKey(name.lexeme)) {
            globalValues.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        if (value == null) {
            values.put(name, UNINTIALISED);

        } else {
            values.put(name, value);
        }
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    Object getAt(int distance, int index) {
        return ancestor(distance).values.get(index);
    }

    void assignAt(int distance, int index, Object value) {
        ancestor(distance).values.set(index, value);
    }

}
