import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class McCluskeyTest {

    public static void main(String[] args) {
        testCase1(); // 3 bits, minterms 0,1,2,3
        testCase2(); // 4 bits, minterms 4,8,10,11,12,15, DC 9,14
        testCase3(); // 2 bits, minterms 0,1,2,3 (Full 1)
        testCase4(); // 2 bits, no minterms (Full 0)
        testCase5(); // 2 bits, single minterm 0
        testCase6(); // 4 bits, 0-7 minterms
    }

    private static void test(String name, String input, String expectedOutput) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            Scanner sc = new Scanner(System.in);
            McCluskeyImpl mcCluskey = new McCluskeyImpl(sc);
            mcCluskey.solve();

            String actualOutput = outContent.toString().trim();
            // Find the last occurrence of ": " which usually follows the last prompt.
            int lastPromptIdx = actualOutput.lastIndexOf(": ");
            String result;
            if (lastPromptIdx != -1) {
                result = actualOutput.substring(lastPromptIdx + 2).trim();
            } else {
                result = actualOutput;
            }

            if (result.equals(expectedOutput)) {
                originalOut.println("PASS: " + name);
            } else {
                originalOut.println("FAIL: " + name);
                originalOut.println("  Expected: " + expectedOutput);
                originalOut.println("  Actual:   " + result);
            }
        } catch (Exception e) {
            originalOut.println("ERROR: " + name + " - " + e.getMessage());
            e.printStackTrace(originalOut);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private static void testCase1() {
        String input = "3\n4\n0\n1\n2\n3\n0\n";
        test("Case 1: 3-bit, 0-3 minterms", input, "x1'");
    }

    private static void testCase2() {
        // 4 bits
        // Minterms: 4, 8, 10, 11, 12, 15
        // DC: 9, 14
        String input = "4\n6\n4\n8\n10\n11\n12\n15\n2\n9\n14\n";
        // The implementation returned "x1x3 + x2x3'x4' + x1x4'"
        test("Case 2: 4-bit complex", input, "x1x3 + x2x3'x4' + x1x4'");
    }

    private static void testCase3() {
        String input = "2\n4\n0\n1\n2\n3\n0\n";
        test("Case 3: Full 1", input, "1");
    }

    private static void testCase4() {
        String input = "2\n0\n0\n";
        test("Case 4: Full 0", input, "0");
    }

    private static void testCase5() {
        String input = "2\n1\n0\n0\n";
        test("Case 5: Single minterm 0", input, "x1'x2'");
    }

    private static void testCase6() {
        String input = "4\n8\n0\n1\n2\n3\n4\n5\n6\n7\n0\n";
        test("Case 6: 4-bit, 0-7 minterms", input, "x1'");
    }
}
