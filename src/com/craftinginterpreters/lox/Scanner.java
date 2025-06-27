package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    public Scanner() {
        this.source = null;
    }

    Scanner(String source) {
        this.source = source;
    }
}

