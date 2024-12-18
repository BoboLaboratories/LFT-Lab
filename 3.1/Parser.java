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

    private void error(String variable) {
        StringBuilder sb = new StringBuilder();
        sb.append("unexpected token <");
        sb.append(look.tag == -1 ? "EOF" : look.tag);
        if (look.getLexeme() != null && !look.getLexeme().isEmpty()) {
            sb.append(", ");
            sb.append(look.getLexeme());
        }
        sb.append("> parsing <");
        sb.append(variable);
        sb.append("> near line ");
        sb.append(lexer.getLine());
        throw new SyntaxError(sb.toString());
    }

    private void match(int tag) {
        if (look.tag == tag) {
            if (look.tag != Tag.EOF) {
                move();
            }
        } else {
            error(Thread.currentThread().getStackTrace()[2].getMethodName());
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
                match('+');
                term();
                exprp();
                break;
            case '-': // <exprp> -> - <term> <exprp>
                match('-');
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
                match('*');
                fact();
                termp();
                break;
            case '/': // <termp> -> / <fact> <termp>
                match('/');
                fact();
                termp();
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
        } catch (SyntaxError e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}