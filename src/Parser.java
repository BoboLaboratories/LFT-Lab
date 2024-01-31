import java.io.*;

public final class Parser {

    private final BufferedReader br;
    private final Lexer lexer;

    private Token look;

    public Parser(Lexer lexer, BufferedReader br) {
        this.lexer = lexer;
        this.br = br;
        move();
    }

    private void move() {
        look = lexer.scan(br);
        System.out.println("token = " + look);
    }

    private void error(String message) {
        throw new Error("near line " + lexer.getLine() + ": " + message);
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
            error("syntax error");
        }
    }

    public void start() {
        switch (look.tag) {
            case '(':
            case Tag.NUM:
                expr();
                match(Tag.EOF);
                break;
            default:
                error("start");
        }
    }


    private void expr() {
        switch (look.tag) {
            case '(':
            case Tag.NUM:
                term();
                exprp();
                break;
            default:
                error("expr");
        }
    }

    private void exprp() {
        switch (look.tag) {
            case '+': // <exprp> -> + <term> <exprp>
                match(Token.PLUS);
                term();
                exprp();
                break;
            case '-': // <exprp> -> - <term> <exprp>
                match(Token.MINUS);
                term();
                exprp();
                break;
            default: // <exprp> -> ε
                break;
        }
    }

    private void term() {
        switch (look.tag) {
            case '(':
            case Tag.NUM:
                fact();
                termp();
                break;
            default:
                error("term");
        }
    }

    private void termp() {
        switch (look.tag) {
            case '*': // <termp> -> * <fact> <termp>
                match(Token.MULT);
                term();
                exprp();
                break;
            case '/': // <termp> -> / <fact> <termp>
                match(Token.DIV);
                term();
                exprp();
                break;
            default: // <termp> -> ε
                break;
        }
    }

    private void fact() {
        switch (look.tag) {
            case '(':
                match('(');
                expr();
                match(')');
                break;
            case Tag.NUM:
                match(Tag.NUM);
                break;
            default:
                error("fact");
                break;
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            Parser parser = new Parser(lex, br);
            parser.start();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}