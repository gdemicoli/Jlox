# Jlox

### Scanner
Scanner's job is to read raw source text and break it into tokens defined by the language.
It outputs a stream of tokens, it doesn't worry about the order of tokens or whether they make sense together. It only checks for lexical errors, which is defined in the scanners token rules.

### Parser
The parser's job is to take the sequence of tokens given by the scanner and build an abstract syntax tree based on the grammar rules.
It is the parser's job to detect syntax errors, valid tokens that are arranged incorrectly.

Expr.java defines the nodes in an AST, they describe the concrete base level data structures.

Parser.java defines grammatical rules for the building blocks defined in Expr.java