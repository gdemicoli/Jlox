# Jlox
A tree-walk interpreter for the Lox programming language, implemented in Java, based on the book *Crafting Interpreters* by Robert Nystrom

This project implements a complete programming language front-to-back, including parsing, static resolution, lexical scoping, closures, functions, classes, and method binding.

## Language Features

The interpreter supports:

- Expressions and statements
- Variables and lexical scoping
- Control flow (`if`, `while`, `for`, `break`)
- First-class functions and closures
- Return statements
- Classes and instances
- Methods and dynamic `this` binding
- Property access and assignment
- Static resolution of local variables

**Scanner**
- Scanner's job is to read raw source text and break it into tokens defined by the language
- It doesn't check the order of tokens or whether they make sense together
- It only checks for lexical errors, which is defined in the scanners token rules
- It outputs a stream of tokens containing information on what they represent

**Parser**
- The parser's job is to take the sequence of tokens given by the scanner and build an abstract syntax tree based on the grammar rules.
- It is the parser's job to detect syntax errors (valid tokens that are arranged incorrectly)
- The parser is implemented using recursive descent, which breaks the languageg grammar down into order of presedence
- It retruns an abstract syntax tree where each node defines a grouping of code

**Resolver**
- Performs a pre pass over the AST, ensuring variables are used correctly
- Resolves variable bindings and scope depth, to speed up variable lookup
- Detects invalid variable usage e.g. reading a variable in its own initializer
- Communicates resolution information to the interpreter

**Interpreter**
- Walks the AST and executes code
- Uses linked Environement objects to model lexical scope
- Uses resolution depth to perform fast variable lookup

## Project Structure
```text
src/
├── com/
│   └── craftinginterpreters/
│       ├── lox/      # Interpreter implementation
│       └── tool/     # AST code generator
└── build/            # Compiled class files

```





## Running the Interpreter

**Prerequisites**

- Java JDK 8+ installed

- javac and java available on your system PATH

Compile:
```bash
javac com/craftinginterpreters/lox/*.java
```
Running a program:
```bash
java -cp build com.craftinginterpreters.lox.Lox path/to/file.lox
```
e.g
```bash
java -cp build com.craftinginterpreters.lox.Lox testCode.lox
```

**Example Scripts can be found in the additional test code folder.**