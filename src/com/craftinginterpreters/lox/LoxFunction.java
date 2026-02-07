package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final List<Token> params;
    private final List<Stmt> body;
    private final String name;
    private final Environment closure;

    private final boolean isInitialiser;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitialiser) {
        this.closure = closure;
        this.params = declaration.params;
        this.body = declaration.body;
        this.name = declaration.name.lexeme;
        this.isInitialiser = isInitialiser;
    }

    LoxFunction(Expr.Function declaration, Environment closure) {
        this.closure = closure;
        this.params = declaration.params;
        this.body = declaration.body;
        this.name = null;
        this.isInitialiser = false;
    }

    private LoxFunction(List<Token> params, List<Stmt> body, String name, Environment closure, boolean isInitialiser) {
        this.params = params;
        this.body = body;
        this.name = name;
        this.closure = closure;
        this.isInitialiser = isInitialiser;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(params, body, name, environment, isInitialiser);
    }

    @Override
    public Object call(Interpreter interpreter,
            List<Object> arguements) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < params.size(); i++) {
            environment.define(params.get(i).lexeme,
                    arguements.get(i));
        }
        try {
            interpreter.executeBlock(body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        if (isInitialiser) {
            return closure.getAt(0, "this");
        }
        return null;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public String toString() {

        return "<fn " + name + ">";
    }

}
