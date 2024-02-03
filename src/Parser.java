import java.io.*;

public final class Parser {

    private final SymbolTable symbols;
    private final CodeGenerator code;
    private final BufferedReader br;
    private final Lexer lexer;

    private Token look;

    enum Op {
        READ,
        ASSIGN,
        PRINT,
        ADD,
        MUL
    };

    private Op op;

    public Parser(Lexer lexer, BufferedReader br) {
        symbols = new SymbolTable();
        code = new CodeGenerator();
        this.lexer = lexer;
        this.br = br;
        move();
    }

    private void move() {
        look = lexer.scan(br);
        // System.out.println("token = " + look);
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
                op = Op.ASSIGN;
                match(Tag.ASSIGN);
                assignlist();
                code.emit(OpCode.GOTO, next);
                op = null;
                break;
            case Tag.PRINT:
                op = Op.PRINT;
                match(Tag.PRINT);
                match('(');
                exprlist();
                match(')');
                code.emit(OpCode.GOTO, next);
                op = null;
                break;
            case Tag.READ:
                op = Op.READ;
                match(Tag.READ);
                match('(');
                idlist();
                match(')');
                code.emit(OpCode.GOTO, next);
                op = null;
                break;
            case Tag.FOR: {
                int trueLabel = code.newLabel();
                int forLabel = code.newLabel();
                match(Tag.FOR);
                match('(');
                statc();
                code.emitLabel(forLabel);
                bexpr(trueLabel, next);
                match(')');
                match(Tag.DO);
                code.emitLabel(trueLabel);
                stat(forLabel);
                break;
            }
            case Tag.IF: {
                int trueLabel = code.newLabel();
                int falseLabel = code.newLabel();
                match(Tag.IF);
                match('(');
                bexpr(trueLabel, falseLabel);
                match(')');
                code.emitLabel(trueLabel);
                stat(next);
                code.emitLabel(falseLabel);
                statp(next);
                match(Tag.END);
                break;
            }
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
                String identifier = look.getLexeme();
                int address = symbols.lookupOrInsert(identifier);
                match(Tag.ID);
                match(Tag.INIT);
                expr();
                code.emit(OpCode.ISTORE, address);
                match(';');
                break;
            case Tag.RELOP:
                break;
            default:
                error("statc");
        }
    }

    private void statp(int next) {
        switch (look.tag) {
            case Tag.ELSE:
                match(Tag.ELSE);
                stat(next);
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
                // reserve variable identifier so that order is kept, then match it
                String identifier = look.getLexeme();
                int address = symbols.lookupOrInsert(identifier);
                match(Tag.ID);

                // make idlistp inherit variable address
                idlistp(address);
                break;
            default:
                error("idlist");
        }
    }

    private void idlistp(int prevAddress) {
        switch (look.tag) {
            case ',':
                match(',');

                // reserve variable identifier so that order is kept, then match it
                String identifier = look.getLexeme();
                int address = symbols.lookupOrInsert(identifier);
                match(Tag.ID);

                // emit actual code for the operation this idlist(p) refers to
                emitIdlistCode(prevAddress, false);

                // make idlistp inherit variable address
                idlistp(address);
                break;
            case ')':
            case ']':
                emitIdlistCode(prevAddress, true);
                break;
            default:
                error("idlistp");
        }
    }

    private void emitIdlistCode(int address, boolean isLast) {
        switch (op) {
            case READ:
                code.emit(OpCode.INVOKESTATIC, 0);
                code.emit(OpCode.ISTORE, address);
                break;
            case PRINT:
                code.emit(OpCode.ILOAD, address);
                code.emit(OpCode.INVOKESTATIC, 1);
                break;
            case ASSIGN:
                // dup the last entry on the stack so that we can perform
                // multiple assigns in a stack efficient manner
                if (!isLast) {
                    code.emit(OpCode.DUP);
                }
                // assign the last dup'ed stack value
                code.emit(OpCode.ISTORE, address);
                break;
        }
    }

    private void bexpr(int trueLabel, int falseLabel) {
        switch (look.tag) {
            case Tag.RELOP:
                Word relop = (Word) look;
                match(Tag.RELOP);
                expr();
                expr();
                switch (relop.getLexeme()) {
                    case "<":  code.emit(OpCode.IF_ICMPLT, trueLabel); break;
                    case ">":  code.emit(OpCode.IF_ICMPGT, trueLabel); break;
                    case "<=": code.emit(OpCode.IF_ICMPLE, trueLabel); break;
                    case ">=": code.emit(OpCode.IF_ICMPGE, trueLabel); break;
                    case "==": code.emit(OpCode.IF_ICMPEQ, trueLabel); break;
                    case "<>": code.emit(OpCode.IF_ICMPNE, trueLabel); break;
                }
                code.emit(OpCode.GOTO, falseLabel);
                break;
            default:
                error("bexpr");
        }
    }

    private void expr() {
        Op opBak;
        switch (look.tag) {
            case '+': {
                opBak = op;
                op = Op.ADD;
                match('+');
                match('(');
                exprlist();
                match(')');
                op = opBak;
                break;
            }
            case '-': {
                opBak = op;
                op = null;
                match('-');
                expr();
                expr();
                code.emit(OpCode.ISUB);
                op = opBak;
                break;
            }
            case '*': {
                opBak = op;
                op = Op.MUL;
                match('*');
                match('(');
                exprlist();
                match(')');
                op = opBak;
                break;
            }
            case '/':
                opBak = op;
                op = null;
                match('/');
                expr();
                expr();
                code.emit(OpCode.IDIV);
                op = opBak;
                break;
            case Tag.NUM:
                int operand = Integer.parseInt(look.getLexeme());
                code.emit(OpCode.LDC, operand);
                match(Tag.NUM);
                break;
            case Tag.ID:
                String identifier = look.getLexeme();
                int address = symbols.lookup(identifier);
                code.emit(OpCode.ILOAD, address);
                match(Tag.ID);
                break;
            default: {
                error("expr");
            }
        }

        if (op == Op.PRINT) {
            code.emit(OpCode.INVOKESTATIC, 1);
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
                switch (op) {
                    case ADD:
                        code.emit(OpCode.IADD);
                        break;
                    case MUL:
                        code.emit(OpCode.IMUL);
                        break;
                    default:
                        break;
                }
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