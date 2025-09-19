package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()), false);

    if (hadError) {
      System.exit(65);
    }
    if (hadRuntimeError) {
      System.exit(70);
    }
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      run(line, true);
      hadError = false;
    }
  }

  private static void run(String source, boolean replMode) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);

    if (replMode) {
      Expr expression = parser.tryParseExpressExpr();

      if (expression != null && !hadError) {
        Object value = interpreter.evaluate(expression);
        System.out.println(interpreter.stringify(value));
        return;
      }
    }

    List<Stmt> statements = parser.parse();

    if (hadError) {
      return;
    }
    // FIX-ME: come back here... we need to finish implement the anoymous function
    // The parser is nearly done, we need to print the Stmt list to check if the
    // anon funcs are being parsed correctly
    // currently the abstract class stmt extended in the interpreter is missing the
    // concrete
    // defininition for visitFunc Stmt DONE: parser is correctly parsing anon funcs
    // as exprs
    // Now need to properly implement visitFuncExpr in interpreter....

    interpreter.interpret(statements);

  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end ", message);
    } else {
      report(token.line, " at ' " + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }
}
