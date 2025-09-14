package com.craftinginterpreters.lox;

import java.util.List;

interface loxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguements);
}
