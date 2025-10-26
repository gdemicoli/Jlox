package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

import com.craftinginterpreters.lox.Stmt.Var;

class VarInfo {
    boolean initialised;
    boolean used;
    Token token;
    int index;

    VarInfo(boolean initialised, boolean used, Token token, int index) {
        this.initialised = initialised;
        this.used = used;
        this.token = token;
        this.index = index;
    }
}

class Scope {
    Map<String, VarInfo> variables = new HashMap<>();
    int nextIndex = 0;
}

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    // private final Stack<Map<String, VarInfo>> scopes = new Stack<>();
    private final Stack<Scope> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private LoopType currentLoop = LoopType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private enum LoopType {
        NONE,
        LOOP
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        resolveFunction(expr, FunctionType.FUNCTION);
        return null;

    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (currentLoop == LoopType.NONE) {
            Lox.error(stmt.keyword, "Break must be inside loop.");
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        LoopType enclosingLoop = currentLoop;
        currentLoop = LoopType.LOOP;

        resolve(stmt.condition);
        resolve(stmt.body);

        currentLoop = enclosingLoop;

        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr arguement : expr.arguments) {
            resolve(arguement);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().variables.get(expr.name.lexeme).initialised == Boolean.FALSE) {
            Lox.error(expr.name, "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        scopes.peek().variables.get(expr.name.lexeme).used = true;
        return null;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void resolveFunction(Expr.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new Scope());
    }

    private void endScope() {
        Scope scope = scopes.pop();
        for (Map.Entry<String, VarInfo> entry : scope.variables.entrySet()) {
            if (!entry.getValue().used) {
                Lox.error(entry.getValue().token, "Variable " + entry.getKey() + " declared but never used.");
            }
        }
    }

    // declares first so that newly declared var isn't used in its own assignment
    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        Scope scope = scopes.peek();
        if (scope.variables.containsKey(name.lexeme)) {
            Lox.error(name, "Already variable with this name in scope.");
        }

        VarInfo info = new VarInfo(false, false, name, scope.nextIndex);
        scope.variables.put(name.lexeme, info);
        scope.nextIndex++;
    }

    // once declaration has passed, the variable can be used safely0
    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        scopes.peek().variables.get(name.lexeme).initialised = true;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).variables.containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i, scopes.get(i).variables.get(name.lexeme).index);
                return;
            }
            // if we walk through all the block scopes & dont find, leave unresolved &
            // assume global
        }
    }

}
