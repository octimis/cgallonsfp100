package www.geeksforgeeks.org;
/*
 * Source: http://www.geeksforgeeks.org/expression-evaluation/
 */
import java.util.Stack;

public class StackInfix {

    private String expression;
    private double result;

    public StackInfix (String expression) {
        expression = formatExpression(expression);
        this.expression = expression;
        result = evaluate(expression);
    }

    public String getExpression() {
        return expression;
    }

    public double getResult() {
        return result;
    }

    private static String formatExpression(String exp) {
        String expFormatted = "";
        // 100*(2+12)
        for (int i = 0; i < exp.length(); i++) {
            char currentChar = exp.charAt(i);
            expFormatted += currentChar;

            // ensure we are not at the last character
            if (i < exp.length() - 1) {
                // parentheses & arithmetic operators must be followed by " "
                if(!isANumber(currentChar)) {
                    expFormatted += " ";
                } else { // currentChar is indeed a number
                    // nextChar is not a number
                    if (!isANumber(exp.charAt(i+1))) {
                        expFormatted += " ";
                    }
                }
            }
        }
        return expFormatted;
    }

    private static boolean isANumber(char asciiValue) {
        return (asciiValue > 47 && asciiValue < 58) ? true : false;
    }

    private static double evaluate(String expression) {
        expression = "0" + expression; // prevents java.util.EmptyStackException
        char[] tokens = expression.toCharArray();

        // Stack for numbers: 'values'
        Stack<Double> values = new Stack<>();

        // Stack for Operators: 'ops'
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++)
        {
            // Current token is a whitespace, skip it
            if (tokens[i] == ' ')
                continue;

            // Current token is a number, push it to stack for numbers
            if (tokens[i] >= '0' && tokens[i] <= '9')
            {
                StringBuffer sbuf = new StringBuffer();
                // There may be more than one digits in number
                while (i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9')
                    sbuf.append(tokens[i++]);
                values.push(Double.parseDouble(sbuf.toString()));
            }

            // Current token is an opening brace, push it to 'ops'
            else if (tokens[i] == '(')
                ops.push(tokens[i]);

                // Closing brace encountered, solve entire brace
            else if (tokens[i] == ')')
            {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.pop();
            }

            // Current token is an operator.
            else if (tokens[i] == '+' || tokens[i] == '-' ||
                    tokens[i] == '*' || tokens[i] == '/' || tokens[i] == '^')
            {
                // While top of 'ops' has same or greater precedence to current
                // token, which is an operator. Apply operator on top of 'ops'
                // to top two elements in values stack
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));

                // Push current token to 'ops'.
                ops.push(tokens[i]);
            }
        }

        // Entire expression has been parsed at this point, apply remaining
        // ops to remaining values
        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));

        // Top of 'values' contains result, return it
        return values.pop();
    }

    // Returns true if 'op2' has higher or same precedence as 'op1',
    // otherwise returns false.
    private static boolean hasPrecedence(char op1, char op2)
    {
        if (op2 == '(' || op2 == ')')
            return false;
        if (op1 == '^')
            return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    // A utility method to apply an operator 'op' on operands 'a'
    // and 'b'. Return the result.
    private static double applyOp(char op, double b, double a)
    {
        switch (op)
        {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new
                            UnsupportedOperationException("Cannot divide by zero");
                return a / b;
            case '^':
                return (int) Math.pow(a, b);
        }
        return 0;
    }
}
