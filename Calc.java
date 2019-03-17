import java.awt.*;
import java.awt.event.*;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.*;

public class Calc
{
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                CalcJFrame frame = new CalcJFrame(); //создаем объект формы JFrame
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}
class CalcJFrame extends JFrame {
    public CalcJFrame() {
        setTitle("Калькулятор");
        CalcJPanel panel = new CalcJPanel(); ////создаем объект панели JFrame
        add(panel);
        pack();
    }
}

class CalcJPanel extends JPanel {

    private JButton display; // поле, где будет выводится выражение
    private JButton display1; // поле, где будет выводиться последнее записанное число
    private JButton displayRes; // поле, где будет выводится результат вычислений
    private JPanel panel;
    private boolean numoroper = true; // проверка оператор или число
    private boolean twooper = false;

    public CalcJPanel() {

        setLayout(new GridLayout(2,1,1,1));

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(5,1,1,1));
        JTextField displayText = new JTextField("Выражение:");
        displayText.setEnabled(false);
        displayPanel.add(displayText);

        display = new JButton("");
        display.setEnabled(false);
        displayPanel.add(display);

        display1 = new JButton("");
        display1.setEnabled(false);
        displayPanel.add(display1);

        JTextField displayResText = new JTextField("Результат:");
        displayResText.setEnabled(false);
        displayPanel.add(displayResText);

        displayRes = new JButton("0");
        displayRes.setEnabled(false);
        displayPanel.add(displayRes);
        add(displayPanel);

        ActionListener numbers = new AddNumber();
        ActionListener operands = new AddOperand();
        ActionListener commands = new GetAction();

        panel = new JPanel();
        panel.setLayout(new GridLayout(4, 6));

        addButton("7", numbers);
        addButton("8", numbers);
        addButton("9", numbers);
        addButton(" / ", operands);
        addButton(" ^ ", operands);
        addButton("<-", commands);

        addButton("4", numbers);
        addButton("5", numbers);
        addButton("6", numbers);
        addButton(" * ", operands);
        addButton("±", commands);
        addButton("C", commands);

        addButton("1", numbers);
        addButton("2", numbers);
        addButton("3", numbers);
        addButton(" - ", operands);
        addButton(" ( ", operands);
        addButton("", commands);

        addButton("0", numbers);
        addButton(".", numbers);
        addButton("=", commands);
        addButton(" + ", operands);
        addButton(" ) ", operands);

        add(panel);
    }

    //функция для создания кнопок
    private void addButton(String label, ActionListener listener) {
        JButton button = new JButton(label);
        button.addActionListener(listener);
        panel.add(button);
    }

    // класс обработки нажания цифровой кнопки или знака (.).
    private class AddNumber implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            String numb = event.getActionCommand();
            display1.setText(display1.getText() + numb);
            numoroper = true;
            twooper = false;
        }
    }

    // класс обработки нажания функциональных кнопок
    private class GetAction implements ActionListener
    {
        public void actionPerformed(ActionEvent event) {
            String act = event.getActionCommand();
            String opn;
            String dispGetT;
            String disp1GetT;
            String outD = display.getText();
            String outD1 = display1.getText();

            switch (act){
                case "C": // сброс дисплеев
                    outD = "";
                    outD1 = "";
                    displayRes.setText("0");
                    break;
                case "<-": // удаление последней цифры вводимого числа
                    outD1 = !(display1.getText().isEmpty()) ? display1.getText().substring(0, display1.getText().length() - 1) : "";
                    break;
                case "=": // расчёт выражения
                    dispGetT = (display.getText().isEmpty() && display1.getText().isEmpty()) ? "0" : display.getText();
                    disp1GetT = display1.getText().equals(".") ? "0.0" : display1.getText();
                    opn = (!(numoroper)) ? getOPN(dispGetT.substring(0, dispGetT.length()-3) + disp1GetT) :
                            getOPN(dispGetT + disp1GetT);
                    displayRes.setText("" + calculateResult(opn));
                    outD = dispGetT + disp1GetT;
                    outD1 = "";
                    break;
                case "±": //преобразование в унарный минус
                    outD1 = (display1.getText().matches(" -(.*)")) ? display1.getText().replaceFirst(" -", "") :
                            " -" + display1.getText();
                default:
                    break;
            }
            display.setText(outD);
            display1.setText(outD1);
        }
    }

    // класс обработки нажания операторных кнопок и круглых скобок.
    private class AddOperand implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            String operand = event.getActionCommand();

            switch (operand) {
                case " ( ":
                    display.setText(display.getText() + operand + display1.getText());
                    break;
                case " ) ":
                    display.setText(display.getText() + display1.getText() + operand);
                    break;
                default:
                    if (!(twooper)) { // проверка на наличие знака перед вводом нового
                        if ((display1.getText().isEmpty()) && display.getText().isEmpty())
                            operand = "0" + operand;
                        display.setText(display.getText() + display1.getText() + operand);
                        String opn = getOPN(display.getText().substring(0, display.getText().length() - 3));
                        displayRes.setText("" + calculateResult(opn));
                        numoroper = false;
                        twooper = true;
                    }
                    break;
            }
            display1.setText("");
        }
    }

    private String getOPN(String inputString) throws EmptyStackException {
        LinkedList<String> opn = new LinkedList<String>();
        Stack<String> stack = new Stack<String>();

        for (String currentSymbol : inputString.split(" ")) {

            //число сразу в выходную строку
            if (isNumber(currentSymbol)) {
                opn.add(currentSymbol + " ");
                continue;
            }

            //скобку сохраним в стеке операций
            if (currentSymbol.equals("(") || stack.empty()) {
                stack.push(currentSymbol);
                continue;
            }

            if (isOperator(currentSymbol)) {
                /*
                 * если верхний в стеке оператор имеет больший
                 * приоритет, чем приоритет текущего оператора, то
                 * извлекаем символы из стека в выходную строку
                 * до тех пор, пока выполняется это условие
                 */
                while (!stack.isEmpty()
                        && priorityOfOperation(stack.peek()) >= priorityOfOperation(currentSymbol))
                    opn.add(stack.pop() + " ");
                stack.push(currentSymbol);
                continue;
            }

            /*
             * если закрывающая скобка, то извлекаем символы из
             * стека операций в выходную строку до тех пор,
             * пока не встретим открывающую скобку.
             */
            if (currentSymbol.equals(")")) {
                while (!stack.peek().equals("("))
                    opn.add(stack.pop() + " ");
                stack.pop();//выталкиваем саму скобку.
                continue;
            }
        }

        /*
         * отложенные в стеке операторы добавляем
         * в выходную строку.
         */
        while (!stack.empty())
            opn.add(stack.pop() + " ");

        //крепим вместе последовательность - и на выход
        StringBuilder sb = new StringBuilder();
        for (String s : opn)
            sb.append(s);

        return sb.toString();
    }

    private boolean isNumber(String currentSymbol) {
        try {
            Double.parseDouble(currentSymbol);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOperator(String c) {
        return c.equals("+") || c.equals("-") || c.equals("*") || c.equals("/")
                || c.equals("^");
    }

    private int priorityOfOperation(String temp) {
        switch (temp) {
            case "^":
                return 3;
            case "/":
            case "*":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return 0;
        }
    }

    private double calculateResult(String OPN) {
        Stack<String> stack = new Stack<String>();

        for (String currentSymbol : OPN.split(" ")) {
            if (isNumber(currentSymbol)) {
                stack.push(currentSymbol);
                continue;
            }

            if (isOperator(currentSymbol)) {
                double result = 0;
                double first = Double.parseDouble(stack.pop());
                double second = Double.parseDouble(stack.pop());

                switch (currentSymbol) {
                    case "^":
                        result = Math.pow(second, first);
                        break;
                    case "/":
                        result = second / first;
                        break;
                    case "*":
                        result = second * first;
                        break;
                    case "+":
                        result = second + first;
                        break;
                    case "-":
                        result = second - first;
                        break;
                }
                stack.push(String.valueOf(result));
            }
        }
        return Double.parseDouble(stack.pop());
    }
}