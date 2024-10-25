package cs2110;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utilities for parsing RPN expressions.
 */
public class RpnParser {

    /**
     * Parse the RPN expression in `exprString` and return the corresponding expression tree. Tokens
     * must be separated by whitespace.  Valid tokens include decimal numbers (scientific notation
     * allowed), arithmetic operators (+, -, *, /, ^), the conditional operator (:?), function names
     * (with the suffix "()"), and variable names (anything else).  When a function name is
     * encountered, the corresponding function will be retrieved from `funcDefs` using the name
     * (without "()" suffix) as the key.
     *
     * @throws IncompleteRpnException     if the expression has too few or too many operands
     *                                    relative to operators and functions.
     * @throws UndefinedFunctionException if a function name applied in `exprString` is not present
     *                                    in `funcDefs`.
     */
    public static Expression parse(String exprString, Map<String, UnaryFunction> funcDefs)
            throws IncompleteRpnException, UndefinedFunctionException {
        assert exprString != null;
        assert funcDefs != null;

        // Each token will result in a subexpression being pushed onto this stack.  If the
        // subexpression requires arguments, they are first popped off of this stack.
        Deque<Expression> stack = new ArrayDeque<>();

        // Loop over each token in the expression string from left to right
        for (Token token : Token.tokenizer(exprString)) {
            // TODO: Based on the dynamic type of the token, create the appropriate Expression node
            //  and push it onto the stack, popping arguments as needed.
            //  The "number" token is done for you as an example.
            try {
                if (token instanceof Token.Number) {
                    Token.Number numToken = (Token.Number) token;
                    stack.push(new Constant(numToken.doubleValue()));
                }
                if (token instanceof Token.Operator) {
                    Token.Operator opToken = (Token.Operator) token;
                    assert Token.Operator.validOperator(opToken.value()); // check if operator is of allowed operations

                    Expression expr2 = stack.pop();
                    Expression expr1 = stack.pop(); // NOTICE the reversed expr order because of LIFO of stack
                    Operation op = new Operation(opToken.opValue(), expr1, expr2);
                    stack.push(op);
                }
                if (token instanceof Token.CondOp) {
                    Token.CondOp condToken = (Token.CondOp) token;
                    Expression fbranch = stack.pop();
                    Expression tbranch = stack.pop();
                    Expression condition = stack.pop(); // the condition, true and false branches will be retrieved in reverse order in LIFO
                    Conditional cond = new Conditional(condition, tbranch, fbranch);
                    stack.push(cond);
                }
                if (token instanceof Token.Function) {
                    Token.Function funcToken = (Token.Function) token;
                    Expression arg = stack.pop();

                    UnaryFunction function = funcDefs.get(funcToken.name());
                        // If a function whose name was not in funcDefs, function would be null and we throw the UndefinedFcuntionException
                    if(function == null) {
                        throw new UndefinedFunctionException(funcToken.name());
                    }

                    Application func = new Application(function, arg);
                    stack.push(func);
                }
                if (token instanceof Token.Variable) {
                    Token.Variable varToken = (Token.Variable) token;
                    Variable var = new Variable(varToken.value());
                    stack.push(var);
                }
            }
            // If the stack was empty and pop() was called, then there was a problem with input with exprString
            // We translate the NoSuchElementException into the more informative IncompleteRpnException
            catch (NoSuchElementException e) {
                throw new IncompleteRpnException(exprString, stack.size());
            }
        }

        // Check if is exactly one invariant
        if (stack.size() != 1) {
            throw new IncompleteRpnException(exprString, stack.size());
        }

        return stack.pop();
    }
}
