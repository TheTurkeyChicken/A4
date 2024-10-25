package cs2110;

import java.util.HashSet;
import java.util.Set;

/**
 * An expression tree node representing a conditional node with two branches.
 */
public class Conditional implements Expression {

    /**
     * The condition to be evaluated for this node.
     */
    private final Expression condition;

    /**
     * The branch to proceed with if the condition does equal 0.
     */
    private final Expression trueBranch;

    /**
     * The branch to proceed with if the condition equals 0.
     */
    private final Expression falseBranch;

    /**
     * Create a node representing the conditional, with condition,
     * true branch, and false branch.
     */
    public Conditional(Expression condition, Expression trueBranch, Expression falseBranch) {
        assert condition != null;
        assert trueBranch != null;
        assert falseBranch != null;

        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    /**
     * Return this node's value. Throws UnboundVariableException if a variable substituted
     * in an expression is not contained in VarTable `vars`.
     */
    @Override
    public double eval(VarTable vars) throws UnboundVariableException {
        assert vars != null;

        if (condition.eval(vars) == 0) {
            return falseBranch.eval(vars);
        } else {
            return trueBranch.eval(vars);
        }
    }

    /**
     * Return the number of operations and unary functions contained in this expression,
     * including the operation of evaluating the condition. Will choose the most expensive
     * branch with more child nodes.
     */
    @Override
    public int opCount() {
        return condition.opCount() + Math.max(trueBranch.opCount(), falseBranch.opCount()) + 1;
    }

    /**
     * Return the infix representation of this expression, enclosing every binary operation in
     * parentheses (regardless of whether they are necessary to preserve the order of operations).
     */
    @Override
    public String infixString() {
        return "(" + condition.infixString() + " ? " + trueBranch.infixString() + " : "
                + falseBranch.infixString() + ")";
    }

    /**
     * Return the postfix representation of this string, separating every token with spaces.
     */
    @Override
    public String postfixString() {
        return condition.postfixString() + " " + trueBranch.postfixString() + " "
                + falseBranch.postfixString() + " ?:";
    }

    @Override
    public Expression optimize(VarTable vars) {
        assert vars != null;

        Expression optCondition = condition.optimize(vars);
        Expression optTrue = trueBranch.optimize(vars);
        Expression optFalse = falseBranch.optimize(vars);

        try {
            if (optCondition.eval(vars) != 0) {
                return optTrue;
            }
            else {
                return optFalse;
            }
        }
        catch (UnboundVariableException e) {
            return new Conditional(optCondition, optTrue, optFalse);
        }
    }


    /**
     * A Condition recurses on all three branches of its condition, trueBranch, and falseBranch,
     * then returns the union of the dependencies of all children.
     */
    @Override
    public Set<String> dependencies() {
        Set<String> condDep = condition.dependencies();
        Set<String> trueDep = trueBranch.dependencies();
        Set<String> falseDep = falseBranch.dependencies();

        Set<String> union = new HashSet<>();
        union.addAll(condDep); union.addAll(trueDep); union.addAll(falseDep);
        return union;
    }

    /**
     * Returns whether `other` is a Conditional of the same class, with the same condition
     * and identical branches in the same order.
     */
    @Override
    public boolean equals(Object other) {
        assert other != null;

        if (this == other)
            return true;
        else if (other.getClass() == this.getClass()) {
            Conditional cond = (Conditional) other;
            return (cond.condition.equals(this.condition) &&
                    cond.trueBranch.equals(this.trueBranch) &&
                    cond.falseBranch.equals(this.falseBranch));
        } else {
            return false;
        }
    }

    /**
     * Return the Conditional as a string in postfix.
     */
    @Override
    public String toString() {
        return postfixString();
    }
}