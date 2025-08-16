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
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case  BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double)right;
        }
        // Unreachable
        return null;
    }
    // fix me: decide on whether to include 0 as false, python style
    private boolean isTruthy(Object object){
        if (object == null){
            return false;
        }
        if(object instanceof  Boolean){
            return (boolean)object;
        }
        return true;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return (double)left - (double)right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                break;
            case SLASH:
                return (double)left / (double)right;
            case  STAR:
                return (double)left * (double)right;
            
        }

        // Unreachable
        return null;
    }
}