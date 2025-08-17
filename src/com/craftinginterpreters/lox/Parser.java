package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0; // points to next token

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }
    // Each method for parsing a grammar rule produces an AST for that rule.
    // When the body of the rule contains a non terminal, we call that other rule's
    // method

    private Expr expression() {
        Expr expr = equality();

        while (match(COMMA)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        // loops through equality operators e.g.: a == b !=c == d
        // then returns when it hits a token that is not equality.
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        // loops through comparison operators each time appending
        // the previous expression to the left hand side of the current one
        // e.g.: a < b <= c = ((a,<,b), <=, c)
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        // Same as above 2 methods, exceptf for terms
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {

        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            // 1. gets (
            // 2. parses inner expression
            // 3. Will throw error up call stack if ) is not present
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression");
    }

    private boolean match(TokenType... types) {
        // checks to see if the current token has any of the given types
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        // returns true if the current token is of the given type (doesn't consume it)
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        // consumes the current token & returns it
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        // Only user for panic mode when parser cannot continue
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronise() {
        // Looks for part of the file
        // which is outside of error boundary to start parsing
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }

    }

}