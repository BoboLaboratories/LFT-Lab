import java.io.*;

public final class Valutatore {

    private final BufferedReader br;
    private final Lexer lexer;

    private Token look;

    public Valutatore(Lexer lexer, BufferedReader br) {
        this.lexer = lexer;
        this.br = br;
        move();
    }

    private void move() {
        look = lexer.scan(br);
        System.out.println("token = " + look);
    }

    private Error error(String message) {
        return new Error("near line " + lexer.getLine() + ": " + message);
    }

    private void match(Token token) {
        match(token.tag);
    }

    private void match(int tag) {
        if (look.tag == tag) {
            if (look.tag != Tag.EOF) {
                move();
            }
        } else {
            throw error("syntax error");
        }
    }

    public void start() {
        switch (look.tag) {
            case '(':
            case Tag.NUM:
                int val = expr();
                System.out.println("Valutatore: " + val);
                match(Tag.EOF);
                break;
            default:
                throw error("start");
        }
    }
    
    private int expr() {
        int val, termVal;
        switch (look.tag) {
            case '(':
            case Tag.NUM:
                termVal = term();
                val = exprp(termVal);
                break;
            default:
                throw error("expr");
        }
        return val;
    }

    private int exprp(int i) {
        int val, termVal;
        switch (look.tag) {
            case '+': // <exprp> -> + <term> <exprp>
                match(Token.PLUS);
                termVal = term();
                val = exprp(i + termVal);
                break;
            case '-': // <exprp> -> - <term> <exprp>
                match(Token.MINUS);
                termVal = term();
                val = exprp(i - termVal);
                break;
            default: // <exprp> -> ε
                val = i;
                break;
        }
        return val;
    }

    private int term() {
        int val, factVal;
        switch (look.tag) {
            case '(':
            case Tag.NUM:
                factVal = fact();
                val = termp(factVal);
                break;
            default:
                throw error("term");
        }
        return val;
    }

    private int termp(int i) {
        int val, factVal;
        switch (look.tag) {
            case '*': // <termp> -> * <fact> <termp>
                match(Token.MULT);
                factVal = fact();
                val = termp(i * factVal);
                break;
            case '/': // <termp> -> / <fact> <termp>
                match(Token.DIV);
                factVal = fact();
                val = termp(i / factVal);
                break;
            default: // <termp> -> ε
                val = i;
                break;
        }
        return val;
    }

    private int fact() {
        int val;
        switch (look.tag) {
            case '(':
                match('(');
                val = expr();
                match(')');
                break;
            case Tag.NUM:
                val = Integer.parseInt(look.getLexeme());
                match(Tag.NUM);
                break;
            default:
                throw error("fact");
        }
        return val;
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            Valutatore valutatore = new Valutatore(lex, br);
            valutatore.start();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}