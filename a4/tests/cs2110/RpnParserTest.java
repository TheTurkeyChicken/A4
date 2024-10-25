package cs2110;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RpnParserTest {

    @Test
    @DisplayName("Parsing an expression consisting of a single number should yield a Constant " +
            "node with that value")
    void testParseConstant() throws IncompleteRpnException, UndefinedFunctionException {
        Expression expr = RpnParser.parse("1.5", Map.of());
        assertEquals(new Constant(1.5), expr);
    }

    @Test
    @DisplayName("Parsing an expression consisting of a single identifier should yield a " +
            "Variable node with that name")
    void testParseVariable() throws IncompleteRpnException, UndefinedFunctionException {
        Expression expr = RpnParser.parse("x", Map.of());
        // TODO: Uncomment this test, adjusting constructor invocations as necessary
        assertEquals(new Variable("x"), expr);
    }

    @Test
    @DisplayName("Parsing an expression ending with an operator should yield an Operation node " +
            "evaluating to the expected value")
    void testParseOperation()
            throws UnboundVariableException, IncompleteRpnException, UndefinedFunctionException {
        Expression expr = RpnParser.parse("1 1 +", Map.of());
        // TODO: Uncomment this test
        assertInstanceOf(Operation.class, expr);
        assertEquals(2.0, expr.eval(MapVarTable.empty()));

        // TODO: This is not a very thorough test!  Both operands are the same, and the operator is
        // commutative.  Write additional test cases that don't have these properties.
        // You should also write a test case that requires recursive evaluation of the operands.

        // Test commutative property
        expr = RpnParser.parse("1 2 -", Map.of());
        assertEquals(-1.0, expr.eval(MapVarTable.empty()));

        // Test nested operations
        expr = RpnParser.parse("2 1 1 0 + + +", Map.of());
        assertEquals(4.0, expr.eval(MapVarTable.empty()));

        expr = RpnParser.parse("y 1 + 2 z * /", Map.of());
        assertEquals(0.5, expr.eval(MapVarTable.of("y", 2.0, "z", 3.0)));
    }

    @Test
    @DisplayName("Parsing an expression ending with a function should yield an Application node " +
            "evaluating to the expected value")
    void testParseApplication()
            throws UnboundVariableException, IncompleteRpnException, UndefinedFunctionException {
        Expression expr = RpnParser.parse("4 sqrt()", UnaryFunction.mathDefs());
        assertInstanceOf(Application.class, expr);
        assertEquals(2.0, expr.eval(MapVarTable.empty()));

        expr = RpnParser.parse("pi 2 / sin()", UnaryFunction.mathDefs());
        assertInstanceOf(Application.class, expr);
        assertEquals(1.0, expr.eval(MapVarTable.of("pi", Math.PI)));
    }

    @Test
    @DisplayName("Parsing an expression ending with the conditional symbol should yield a "
            + "Conditional node evaluating to the expected value")
    void testParseConditional()
            throws UnboundVariableException, IncompleteRpnException, UndefinedFunctionException {
        Expression expr = RpnParser.parse("1 2 3 ?:", UnaryFunction.mathDefs());
        // TODO: Uncomment this test
        assertInstanceOf(Conditional.class, expr);
        assertEquals(2.0, expr.eval(MapVarTable.empty()));

        expr = RpnParser.parse("x 3.0 + 2.0 y * 7.0 ?:", UnaryFunction.mathDefs());
        assertInstanceOf(Conditional.class, expr);
        assertEquals(8.0, expr.eval(MapVarTable.of("x", 1.0, "y", 4.0)));
        assertEquals(7.0, expr.eval(MapVarTable.of("x", -3.0, "y", 4.0)));

        // ((x - 3) ? sin(y / 2) : cos(y))
        expr = RpnParser.parse("x 3 - y 2 / sin() y cos() ?:", UnaryFunction.mathDefs());
        assertInstanceOf(Conditional.class, expr);
        assertEquals(1.0, expr.eval(MapVarTable.of("x", 1.0, "y", Math.PI)));
        assertEquals(-1.0, expr.eval(MapVarTable.of("x", 3.0, "y", Math.PI)));
    }

    @Test
    @DisplayName("Parsing an empty expression should throw an IncompleteRpnException")
    void testParseEmpty() {
        assertThrows(IncompleteRpnException.class, () -> RpnParser.parse("", Map.of()));
    }

    @Test
    @DisplayName("Parsing an expression that leave more than one term on the stack should throw " +
            "an IncompleteRpnException")
    void testParseIncomplete() {
        assertThrows(IncompleteRpnException.class, () -> RpnParser.parse("1 1 1 +", Map.of()));
    }

    @Test
    @DisplayName("Parsing an expression that consumes more terms than are on the stack should " +
            "throw an IncompleteRpnException")
    void testParseUnderflow() {
        assertThrows(IncompleteRpnException.class, () -> RpnParser.parse("1 1 + +", Map.of()));
        assertThrows(IncompleteRpnException.class, () -> RpnParser.parse("sin() +", Map.of()));
    }

    @Test
    @DisplayName("Parsing an expression that applies an unknown function should throw an " +
            "UnknownFunctionException")
    void testParseUndefined() {
        assertThrows(UndefinedFunctionException.class, () -> RpnParser.parse("1 foo()", Map.of()));
        assertThrows(UndefinedFunctionException.class, () -> RpnParser.parse("x bar()", Map.of()));
    }
}
