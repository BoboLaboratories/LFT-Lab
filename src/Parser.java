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
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':
                statlist();
                match(Tag.EOF);
                break;
            default:
                error("start");
        }
    }

    private void statlist() {
        switch (look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':
                stat();
                statlistp();
                break;
            default:
                error("statlist");
        }
    }

    private void statlistp() {
        switch (look.tag) {
            case ';':
                match(';');
                stat();
                statlistp();
                break;
            case Tag.EOF:
            case '}':
                break;
            default:
                error("statlistp");
        }
    }

    private void stat() {
        switch (look.tag) {
            case Tag.ASSIGN:
                match(Tag.ASSIGN);
                assignlist();
                break;
            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                exprlist();
                match(')');
                break;
            case Tag.READ:
                match(Tag.READ);
                match('(');
                idlist();
                match(')');
                break;
            case Tag.FOR:
                match(Tag.FOR);
                match('(');
                statc();
                bexpr();
                match(')');
                match(Tag.DO);
                stat();
                break;
            case Tag.IF:
                match(Tag.IF);
                match('(');
                bexpr();
                match(')');
                stat();
                statp();
                match(Tag.END);
                break;
            case '{':
                match('{');
                statlist();
                match('}');
                break;
            default:
                error("stat");
        }
    }

    private void statc() {
        switch (look.tag) {
            case Tag.ID:
                match(Tag.ID);
                match(Tag.INIT);
                expr();
                match(';');
                break;
            case Tag.RELOP:
                break;
            default:
                error("statc");
        }
    }

    private void statp() {
        switch (look.tag) {
            case Tag.ELSE:
                match(Tag.ELSE);
                stat();
                break;
            case Tag.END:
                break;
            default:
                error("statp");
        }
    }

    private void assignlist() {
        switch (look.tag) {
            case '[':
                match('[');
                expr();
                match(Tag.TO);
                idlist();
                match(']');
                assignlistp();
                break;
            default:
                error("assignlist");
        }
    }

    private void assignlistp() {
        switch (look.tag) {
            case '[':
                match('[');
                expr();
                match(Tag.TO);
                idlist();
                match(']');
                assignlistp();
                break;
            case ';':
            case Tag.ELSE:
            case Tag.END:
            case Tag.EOF:
            case '}':
                break;
            default:
                error("assignlistp");
        }
    }

    private void idlist() {
        switch (look.tag) {
            case Tag.ID:
                match(Tag.ID);
                idlistp();
                break;
            default:
                error("idlist");
        }
    }

    private void idlistp() {
        switch (look.tag) {
            case ',':
                match(',');
                match(Tag.ID);
                idlistp();
                break;
            case ')':
            case ']':
                break;
            default:
                error("idlistp");
        }
    }

    private void bexpr() {
        switch (look.tag) {
            case Tag.RELOP:
                match(Tag.RELOP);
                expr();
                expr();
                break;
            default:
                error("bexpr");
        }
    }

    private void expr() {
        switch (look.tag) {
            case '+':
                match('+');
                match('(');
                exprlist();
                match(')');
                break;
            case '-':
                match('-');
                expr();
                expr();
                break;
            case '*':
                match('*');
                match('(');
                exprlist();
                match(')');
                break;
            case '/':
                match('/');
                expr();
                expr();
                break;
            case Tag.NUM:
                match(Tag.NUM);
                break;
            case Tag.ID:
                match(Tag.ID);
                break;
            default:
                error("expr");
        }
    }

    private void exprlist() {
        switch (look.tag) {
            case '+':
            case '-':
            case '*':
            case '/':
            case Tag.NUM:
            case Tag.ID:
                expr();
                exprlistp();
                break;
            default:
                error("exprlist");
        }
    }

    private void exprlistp() {
        switch (look.tag) {
            case ',':
                match(',');
                expr();
                exprlistp();
                break;
            case ')':
                break;
            default:
                error("exprlistp");
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