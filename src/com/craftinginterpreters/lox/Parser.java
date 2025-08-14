package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0; // points to next token

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    //Each method for parsing a grammar rule produces an AST for that rule.
    //When the body of the rule contains a non terminal, we call that other rule's method

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
}