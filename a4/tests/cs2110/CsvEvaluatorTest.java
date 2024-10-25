package cs2110;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CsvEvaluatorTest {

    @Test
    @DisplayName("The column label for column 0 should be the empty string")
    void testColToLetters0() {
        assertEquals("", CsvEvaluator.colToLetters(0));
    }

    @Test
    @DisplayName("Column labels for columns 1-26 should consist of the correct single letter")
    void testColToLetters1() {
        assertEquals("A", CsvEvaluator.colToLetters(1));
        assertEquals("Z", CsvEvaluator.colToLetters(26));
    }

    @Test
    @DisplayName("Column labels for columns 27-702 should consist of the correct two letters")
    void testColToLetters2() {
        assertEquals("AA", CsvEvaluator.colToLetters(27));
        assertEquals("AB", CsvEvaluator.colToLetters(28));
        assertEquals("ZY", CsvEvaluator.colToLetters(701));
        assertEquals("ZZ", CsvEvaluator.colToLetters(702));
    }

    @Test
    @DisplayName("Column labels for columns 703-18278 should consist of the correct three letters")
    void testColToLetters3() {
        assertEquals("AAA", CsvEvaluator.colToLetters(703));
        assertEquals("AAB", CsvEvaluator.colToLetters(704));
        assertEquals("AMJ", CsvEvaluator.colToLetters(1024));
        assertEquals("XFD", CsvEvaluator.colToLetters(16384));
        assertEquals("ZZY", CsvEvaluator.colToLetters(18277));
        assertEquals("ZZZ", CsvEvaluator.colToLetters(18278));
    }



    @Test
    @DisplayName("A spreadsheet containing only constants should not be modified when evaluating " +
            "its formulas")
    void testEvaluateCsvConstant() throws IOException {
        String input = "x,1.5\n";
        String expected = "x,1.5\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet with a formula referencing a cell on a previous row should " +
            "evaluate correctly.")
    void testEvaluateCsvAboveRef() throws IOException {
        String input = "x,1.5\n" +
                "y,=B1 4 * 1 +\n";
        String expected = "x,1.5\n"
                + "y,7.0\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet with a formula referencing a previous cell on the same row " +
            "should evaluate correctly.")
    void testEvaluateCsvLeftRef() throws IOException {
        String input = "x,1.5,=B1 4 * 1 +\n";
        String expected = "x,1.5,7.0\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet with a formula referencing a previous formula should evaluate " +
            "correctly.")
    void testEvaluateCsvFormulaRef() throws IOException {
        String input = "x,1.5\n" +
                "y,=B1 4 * 1 +\n" +
                "z,=B1 B2 *\n";
        String expected = "x,1.5\n"
                + "y,7.0\n"
                + "z,10.5\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula referencing a cell that does not contain a number should " +
            "evaluate to #N/A.")
    void testEvaluateCsvNonNumericRef() throws IOException {
        String input = "x,1.5\n" +
                "w,=A1\n";
        String expected = "x,1.5\n"
                + "w,#N/A\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should correctly evaluate a conditional expression.")
    void testEvaluateConditional() throws IOException {
        String input = "x,1.5\n" +
                "3,=B1 2 * A2 -\n" +
                "2,=B2 B1 A3 ?:\n";
        String expected = "x,1.5\n" +
                "3,0.0\n" +
                "2,2.0\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should correctly evaluate an application expression.")
    void testEvaluateApplication() throws IOException {
        String input = "x,1.5\n" +
                "3,=B1 4 * A2 +\n" +
                "2,=B2 sqrt()\n";
        String expected = "x,1.5\n" +
                "3,9.0\n" +
                "2,3.0\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula evaluating an known application expression" +
            "should evaluate to #N/A")
    void testEvaluateUnknownApplication() throws IOException {
        String input = "x,1.5\n" +
                "3,=B1 4 * A2 +\n" +
                "2,=B2 foo()\n";
        String expected = "x,1.5\n" +
                "3,9.0\n" +
                "2,#N/A\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should evaluate to #N/A for references to future cells.")
    void testFutureReferences() throws IOException {
        String input = "=A3 B1 +,=B3\n" +
                "3,=A2 3 *\n" +
                "=A3,=B2 sqrt()\n";
        String expected = "#N/A,#N/A\n" +
                "3,9.0\n" +
                "#N/A,3.0\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should evaluate to #N/A for out-of-bound cell references.")
    void testOutOfBoundReferences() throws IOException {
        String input = "=A4 B1 +,1.5\n" +
                "3,=A2 3 *\n" +
                "=C1,=C4\n";
        String expected = "#N/A,1.5\n" +
                "3,9.0\n" +
                "#N/A,#N/A\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should evaluate to #N/A for variables which do not" +
            "correspond to a valid cell.")
    void testInvalidVariables() throws IOException {
        String input = "x,1.5\n" +
                "3,=x 4 * A2 +\n" +
                "2,=B2 sin()\n";
        String expected = "x,1.5\n" +
                "3,#N/A\n" +
                "2,#N/A\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should evaluate to #N/A for incomplete RPN expressions.")
    void testIncompleteRPNExpression() throws IOException {
        String input = "x,=1.5+\n" +
                "3,=A2 1.5 +\n" +
                "2,=B2 A3\n";
        String expected = "x,#N/A\n" +
                "3,4.5\n" +
                "2,#N/A\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }

    @Test
    @DisplayName("A spreadsheet formula should evaluate to Infinity when dividing by 0, and" +
            "NaN when 0 is divided by 0.")
    void testDivideByZero() throws IOException {
        String input = "x,=1 0 /\n" +
                "3,=0 0 /\n" +
                "2,y\n";
        String expected = "x,Infinity\n" +
                "3,NaN\n" +
                "2,y\n";

        StringBuilder output = new StringBuilder();
        CsvEvaluator.evaluateCsv(CsvEvaluator.SIMPLIFIED_CSV.parse(new StringReader(input)),
                CsvEvaluator.SIMPLIFIED_CSV.print(output));
        assertEquals(expected, output.toString());
    }
}
