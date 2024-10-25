package cs2110;

import java.util.HashSet;
import java.util.Set;

public class Variable implements Expression{

    /**
     * The name of this expression.
     */
    private final String name;

    /**
     * Create a node representing the variable with `name`.
     */
    public Variable(String name) {
        assert name != null;

        this.name = name;
    }

    /**
     * Return this node's value. Throws UnboundVariableException if the variable
     * is not contained in VarTable `vars`.
     */
    @Override
    public double eval(VarTable vars) throws UnboundVariableException {
        assert vars != null;

        return vars.get(name);
    }

    /**
     * No operations are required to evaluate a Variable's value.
     */
    @Override
    public int opCount() {
        return 0;
    }

    /**
     * Return the decimal representation of this node's value (with sufficient precision to
     * reproduce its binary value).
     */
    @Override
    public String infixString() {
        return name;
    }

    /**
     * Return the decimal representation of this node's value (with sufficient precision to
     * reproduce its binary value).
     */
    @Override
    public String postfixString() {
        return name;
    }

    /**
     * Return self (a Variable is already fully optimized).
     */
    @Override
    public Expression optimize(VarTable vars) {
        assert vars != null;
        try {
            return new Constant(vars.get(name));
        } catch (UnboundVariableException e) {
            return this;
        }
    }

    /**
     * A Variable has exactly itself as a dependency.
     */
    @Override
    public Set<String> dependencies() {
        return Set.of(name);
    }

    /**
     * Returns whether `other` is a Variable of the same class, and whether they share
     * identical names.
     */
    @Override
    public boolean equals(Object other) {
        assert other != null;

        if(this == other)
            return true;
        else if(other.getClass() == this.getClass()) {
            Variable var = (Variable) other;
            return var.name.equals(this.name);
        }
        else {
            return false;
        }
    }

    /**
     * Return the Variable as a string in postfix.
     */
    @Override
    public String toString() {
        return postfixString();
    }
}
