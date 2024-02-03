import java.io.*;

public final class Parser {

    private final SymbolTable symbols;
    private final CodeGenerator code;
    private final BufferedReader br;
    private final Lexer lexer;

    private Token look;

    enum IdListOp {
        LOAD,
        STORE,
        READ,
    };

    private boolean print = false;

    private IdListOp idListOp;

    public Parser(Lexer lexer, BufferedReader br) {
        symbols = new SymbolTable();
        code = new CodeGenerator();
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

    public void prog() {
        switch (look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':
                int next = code.newLabel();
                statlist(next);
                code.emitLabel(next);
                match(Tag.EOF);
                try {
                    code.toJasmin();
                } catch (IOException e) {
                    System.out.println("IO error\n");
                }
                break;
            default:
                error("start");
        }
    }

    private void statlist(int next) {
        switch (look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':
                int statNext = code.newLabel();
                stat(statNext);
                code.emit(OpCode.GOTO, statNext);
                code.emitLabel(statNext);
                statlistp(next);
                break;
            default:
                error("statlist");
        }
    }

    private void statlistp(int next) {
        switch (look.tag) {
            case ';':
                match(';');
                int nextListP = code.newLabel();
                stat(nextListP);
                code.emitLabel(nextListP);
                statlistp(next);
                break;
            case Tag.EOF:
            case '}':
                code.emit(OpCode.GOTO, next);
                break;
            default:
                error("statlistp");
        }
    }

    private void stat(int next) {
        switch (look.tag) {
            case Tag.ASSIGN:
                match(Tag.ASSIGN);
                assignlist();
                break;
            case Tag.PRINT:
                print = true;
                match(Tag.PRINT);
                match('(');
                exprlist();
                match(')');
                print = false;
                break;
            case Tag.READ:
                idListOp = IdListOp.READ;
                match(Tag.READ);
                match('(');
                idlist();
                match(')');
                idListOp = null;
                break;
            case Tag.FOR:
                match(Tag.FOR);
                match('(');
                statc();
                bexpr();
                match(')');
                match(Tag.DO);
                stat(next);
                break;
            case Tag.IF:
                match(Tag.IF);
                match('(');
                bexpr();
                match(')');
                stat(next);
                statp();
                match(Tag.END);
                break;
            case '{':
                match('{');
                statlist(next);
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
                stat(-1);
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
                idListOp = IdListOp.STORE;
                match('[');
                expr();
                match(Tag.TO);
                idlist();
                match(']');
                assignlistp();
                idListOp = null;
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
                String identifier = look.getLexeme();
                match(Tag.ID);
                idlistp();
                emitIdentifier(identifier);
                break;
            default:
                error("idlist");
        }
    }

    private void idlistp() {
        switch (look.tag) {
            case ',':
                match(',');
                String identifier = look.getLexeme();
                match(Tag.ID);
                if (idListOp != IdListOp.READ) {
                    code.emit(OpCode.DUP);
                }
                idlistp();
                emitIdentifier(identifier);
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
                Word relop = (Word) look;
                match(Tag.RELOP);
                expr();
                expr();
                switch (relop.getLexeme()) {
                    case "<":  code.emit(OpCode.IF_ICMPLT); break;
                    case ">":  code.emit(OpCode.IF_ICMPGT); break;
                    case "<=": code.emit(OpCode.IF_ICMPLE); break;
                    case ">=": code.emit(OpCode.IF_ICMPGE); break;
                    case "==": code.emit(OpCode.IF_ICMPEQ); break;
                    case "<>": code.emit(OpCode.IF_ICMPNE); break;
                }
                break;
            default:
                error("bexpr");
        }
    }

    private void expr() {
        boolean printBak;
        switch (look.tag) {
            case '+':
                printBak = print;
                print = false;
                match('+');
                match('(');
                exprlist();
                match(')');
                code.emit(OpCode.IADD);
                if ((print = printBak)) {
                    code.emit(OpCode.INVOKESTATIC, 1);
                }
                break;
            case '-':
                printBak = print;
                print = false;
                match('-');
                expr();
                expr();
                code.emit(OpCode.ISUB);
                if ((print = printBak)) {
                    code.emit(OpCode.INVOKESTATIC, 1);
                }
                break;
            case '*':
                printBak = print;
                print = false;
                match('*');
                match('(');
                exprlist();
                match(')');
                code.emit(OpCode.IMUL);
                if ((print = printBak)) {
                    code.emit(OpCode.INVOKESTATIC, 1);
                }
                break;
            case '/':
                printBak = print;
                print = false;
                match('/');
                expr();
                expr();
                code.emit(OpCode.IDIV);
                if ((print = printBak)) {
                    code.emit(OpCode.INVOKESTATIC, 1);
                }
                break;
            case Tag.NUM:
                int operand = Integer.parseInt(look.getLexeme());
                code.emit(OpCode.LDC, operand);
                match(Tag.NUM);
                if (print) {
                    code.emit(OpCode.INVOKESTATIC, 1);
                }
                break;
            case Tag.ID:
                String identifier = look.getLexeme();
                int address = symbols.lookup(identifier);
                System.out.println("Matching ID: " + identifier + " with address: " + address);
                code.emit(OpCode.ILOAD, address);
                match(Tag.ID);
                if (print) {
                    code.emit(OpCode.INVOKESTATIC, 1);
                }
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

    private void emitIdentifier(String identifier) {
        switch (idListOp) {
            case LOAD: {
                int address = symbols.lookup(identifier);
                code.emit(OpCode.ISTORE, address);
                break;
            }
            case STORE: {
                int address = symbols.lookupOrInsert(identifier);
                code.emit(OpCode.ISTORE, address);
                break;
            }
            case READ: {
                int address = symbols.lookupOrInsert(identifier);
                code.emit(OpCode.INVOKESTATIC, 0);
                code.emit(OpCode.ISTORE, address);
                break;
            }
        }

    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            Parser parser = new Parser(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch (SyntaxError e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}