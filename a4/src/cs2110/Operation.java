package cs2110;

import java.util.HashSet;
import java.util.Set;

/**
 * An expression tree node representing an operation between two operands.
 */
public class Operation implements Expression{

    /**
     * The operator of the expression. Can be ADD, SUBTRACT, MULTIPLY,
     * DIVIDE, or POW.
     */
    private final Operator op;

    /**
     * The left operand of the operation.
     */
    private final Expression leftOperand;

    /**
     * The right operand of the operation.
     */
    private final Expression rightOperand;

    /**
     * Create a node representing the operation, with its operator, left expression
     * child, and the right expression child.
     */
    public Operation(Operator op, Expression left, Expression right) {
        assert op != null;
        assert left != null && right != null;

        this.op = op;
        this.leftOperand = left;
        this.rightOperand = right;
    }

    /**
     * Return this node's value. Throws UnboundVariableException if a variable substituted
     * in an expression is not contained in VarTable `vars`.
     */
    @Override
    public double eval(VarTable vars) throws UnboundVariableException {
        assert vars != null;

        return op.operate(leftOperand.eval(vars), rightOperand.eval(vars));
    }

    /**
     * Return the number of operations and unary functions contained in this expression.
     */
    @Override
    public int opCount() {
        return leftOperand.opCount() + rightOperand.opCount() + 1;
    }

    /**
     * Return the infix representation of this expression, enclosing every binary operation in
     * parentheses (regardless of whether they are necessary to preserve the order of operations).
     */
    @Override
    public String infixString() {
        return "(" + leftOperand.infixString() + " " + op.symbol() + " " + rightOperand.infixString() + ")";
    }

    /**
     * Return the postfix representation of this string, separating every token with spaces.
     */
    @Override
    public String postfixString() {
        return leftOperand.postfixString() + " " + rightOperand.postfixString() + " " + op.symbol();
    }

    /**
     *
     */
    @Override
    public Expression optimize(VarTable vars) {
        assert vars != null;

        Expression leftOptimal = leftOperand.optimize(vars);
        Expression rightOptimal = rightOperand.optimize(vars);
        try {
            return new Constant(op.operate(leftOptimal.eval(vars), rightOptimal.eval(vars)));
        } catch (UnboundVariableException e) {
            return new Operation(op, leftOptimal, rightOptimal);
        }
    }

    /**
     * An Operation recurses on both branches of its operands, then returns the union of the
     * dependencies of both children.
     */
    @Override
    public Set<String> dependencies() {
        Set<String> leftDep = leftOperand.dependencies();
        Set<String> rightDep = rightOperand.dependencies();

        Set<String> union = new HashSet<>(); union.addAll(leftDep); union.addAll(rightDep);
        return union;
    }

    /**
     * Returns whether `other` is an Operation of the same class, with the same operator
     * and identical operands in the same order.
     */
    public boolean equals(Object other) {
        assert other != null;

        if(this == other)
            return true;
        else if(other.getClass() == this.getClass()) {
            Operation oper = (Operation) other;
            return (oper.op.symbol().equals(this.op.symbol()) &&
                    oper.leftOperand.equals(this.leftOperand) &&
                    oper.rightOperand.equals(this.rightOperand));
        } else {
            return false;
        }
    }

    /**
     * Return the Operation as a string in postfix.
     */
    @Override
    public String toString() {
        return postfixString();
    }
}
