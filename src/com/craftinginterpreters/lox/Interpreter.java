package com.craftinginterpreters.lox;
// EXAMPLE evaluation of literal:

// 1. evaluate(literalExpr) is called which runs literal.accept(this)
// 2. which runs Interpreter.visitLiteralExpr(Literal expr)
// 3. the visit method for visit literalExpr returns expr.value

// EXAMPLE evaluation of Unary -5:

// 1. evaluate(unaryExpr) is called which runs unary.accept(this)
// 2. which runs Interpreter.visitUnaryExpr(Unary expr)
// 3. the visit method for visit UnaryExpr calls evaluate(expr.right)
//    which is the same as expr.right.accept(this)
// 4. which then calls the visit literal method here, 6 will be returned
// 5. Back within the visit method in step 3 the switch makes it negative
// 6. -6 is returned
class Interpreter implements Expr.Visitor<Object> {

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        // Unreachable
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    // fix me: decide on whether to include 0 as false, python style
    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);
            case EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                // checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
                break;
            case SLASH:
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;

        }

        // Unreachable
        return null;
    }
}