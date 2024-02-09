import java.io.*;
import java.util.function.BiConsumer;

/*
 * Sopra i metodi che eseguono il match di ciascuna variabile
 * é riportato, in commento, il rispettivo SDT.
 *
 * Per consentire una formalizzazione precisa dell'SDT,
 * definiamo la funzione emitOpIfIn(e, p, S), i cui argomenti sono:
 *      - e ∈ Translator.Op | un emettitore di codice
 *      - p ∈ int           | un argomento da passare a e
 *      - S ⊆ Translator.Op | un insieme di emettitori di codici
 *
 * E si ha che
 * 
 *      x(p) ⟺ ∃ x ∈ S | (x = e) ∨ ((e = ASSIGN) ∧ (x = ASSIGN_LAST))
 *
 * dove x(p) indica l'invocazione dell'emettitore di codice x con parametro p.
 *
 */
public final class Translator {

    private final SymbolTable symbols;
    private final CodeGenerator code;
    private final BufferedReader br;
    private final Lexer lexer;

    private Token look;

    public Translator(Lexer lexer, BufferedReader br) {
        this.symbols = new SymbolTable();
        this.code = new CodeGenerator();
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

    /*
     * <prog> -> { statlist.next = newLabel() }
     *           <statlist>
     *           { emitLabel(statlist.next) }
     *           EOF
     */
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
                break;
            default:
                error("start");
        }
    }

    /*
     * <statlist> -> { stat.next = newLabel() }
     *               <stat>
     *               { emitLabel(stat.next) }
     *               { statlistp.next = statlist.next }
     *               <statlistp>
     */
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

    /*
     * <statlistp> -> ;
     *                { stat.next = newLabel() }
     *                <stat>
     *                { emitLabel(stat.next) }
     *                { statlistp1.next = statlistp.next }
     *                <statlistp1>
     *
     * <statlistp> -> ε
     *                { emit(GOTO, statlistp.next }
     */
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

    /*
     * <stat> -> assign <assignlist>
     *           { emit(GOTO, stat.next) }
     *
     * <stat> -> print (
     *           { exprlist.op = PRINT }
     *           <exprlist> )
     *           { emit(GOTO, stat.next)
     *
     * <stat> -> read (
     *           { idlist.op = READ }
     *           <idlist> )
     *           { emit(GOTO, stats.next }
     *
     * <stat> -> for ( <statc>
     *           { stat1.next = newLabel() }
     *           { emitLabel(stat1.next) }
     *           { bexpr.trueLabel = newLabel() }
     *           { bexpr.falseLabel = stat.next }
     *           <bexpr> ) do
     *           { emitLabel(bexpr.trueLabel) }
     *           <stat1>
     *
     * <stat> -> if (
     *           { bexpr.trueLabel = newLabel() }
     *           { bexpr.falseLabel = newLabel() }
     *           <bexpr> )
     *           { emitLabel(bexpr.trueLabel) }
     *           { stat1.next = stat.next }
     *           <stat1>
     *           { emitLabel(bexpr.falseLabel) }
     *           { statp.next = stat.next }
     *           <statp> end
     *
     * <stat> -> {
     *           { statlist.next = stat.next }
     *           <statlist> }
     */
    private void stat(int next) {
        switch (look.tag) {
            case Tag.ASSIGN:
                match(Tag.ASSIGN);
                assignlist();
                code.emit(OpCode.GOTO, next);
                break;
            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                exprlist(Op.PRINT);
                match(')');
                code.emit(OpCode.GOTO, next);
                break;
            case Tag.READ:
                match(Tag.READ);
                match('(');
                idlist(Op.READ);
                match(')');
                code.emit(OpCode.GOTO, next);
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

    /*
     * <statc> -> ID :=
     *            { expr.op = NONE }
     *            <expr>
     *            { emit(ISTORE, lookupOrInsert(ID) }
     *            ;
     *
     * <statc> -> ε
     */
    private void statc() {
        switch (look.tag) {
            case Tag.ID:
                String identifier = look.getLexeme();
                int address = symbols.lookupOrInsert(identifier);
                match(Tag.ID);
                match(Tag.INIT);
                expr(Op.NONE);
                code.emit(OpCode.ISTORE, address);
                match(';');
                break;
            case Tag.RELOP:
            case Tag.AND:
            case Tag.OR:
            case '!':
                break;
            default:
                error("statc");
        }
    }

    /*
     * <statp> -> else
     *            { stat.next = statp.next }
     *            <stat>
     *
     * <statp> -> ε
     */
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

    /*
     * <assignlist> -> [
     *                 { expr.op = ASSIGN }
     *                 <expr> to
     *                 { idlist.op = ASSIGN }
     *                 <idlist> ] <assignlistp>
     */
    private void assignlist() {
        switch (look.tag) {
            case '[':
                match('[');
                expr(Op.ASSIGN);
                match(Tag.TO);
                idlist(Op.ASSIGN);
                match(']');
                assignlistp();
                break;
            default:
                error("assignlist");
        }
    }

    /*
     * <assignlistp> -> [
     *                  { expr.op = ASSIGN }
     *                  <expr> to
     *                  { idlist.op = ASSIGN }
     *                  <idlist> ] <assignlistp1>
     *
     * <assignlistp> -> ε
     */
    private void assignlistp() {
        switch (look.tag) {
            case '[':
                match('[');
                expr(Op.ASSIGN);
                match(Tag.TO);
                idlist(Op.ASSIGN);
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

    /*
     * <idlist> -> ID
     *             { idlistp.prevAddress = lookupOrInsert(ID) }
     *             { idlistp.op = idlist.op }
     *             <idlistp>
     */
    private void idlist(Op op) {
        switch (look.tag) {
            case Tag.ID:
                // reserve variable identifier so that order is kept, then match it
                String identifier = look.getLexeme();
                int address = symbols.lookupOrInsert(identifier);
                match(Tag.ID);

                // make idlistp inherit variable address
                idlistp(op, address);
                break;
            default:
                error("idlist");
        }
    }

    /*
     * <idlistp> -> , ID
     *              { emitOpIfIn(idlistp.op, idlistp.prevAddress, { READ, PRINT, ASSIGN }) }
     *              { idlistp1.prevAddress = lookupOrInsert(ID) }
     *              { idlistp1.op = idlistp.op }
     *              <idlistp1>
     *
     * <idlistp> -> ε
     *              { emitOpIfIn(idlistp.op, idlistp.prevAddress, { READ, PRINT, ASSIGN_LAST }) }
     */
    private void idlistp(Op op, int prevAddress) {
        switch (look.tag) {
            case ',':
                match(',');
                // reserve variable identifier so that order is kept, then match it
                String identifier = look.getLexeme();
                int address = symbols.lookupOrInsert(identifier);
                match(Tag.ID);

                // emit actual code for the operation this idlist(p) refers to
                code.emitOpIfIn(op, prevAddress, Op.READ, Op.PRINT, Op.ASSIGN);

                // make idlistp inherit variable address
                idlistp(op, address);
                break;
            case ')':
            case ']':
                code.emitOpIfIn(op, prevAddress, Op.READ, Op.PRINT, Op.ASSIGN_LAST);
                break;
            default:
                error("idlistp");
        }
    }

    /*
     * <bexpr> -> <
     *            { expr1.op = NONE }
     *            <expr1>
     *            { expr2.op = NONE }
     *            <expr2>
     *            { emit(IF_ICMPLT, bexpr.trueLabel) }
     *            { emit(GOTO, bexpr.falseLabel) }
     *
     * <bexpr> -> >
     *            { expr1.op = NONE }
     *            <expr1>
     *            { expr2.op = NONE }
     *            <expr2>
     *            { emit(IF_CMPGT, bexpr.trueLabel) }
     *            { emit(GOTO, bexpr.falseLabel) }
     *
     * <bexpr> -> <=
     *            { expr1.op = NONE }
     *            <expr1>
     *            { expr2.op = NONE }
     *            <expr2>
     *            { emit(IF_CMPLE, bexpr.trueLabel) }
     *            { emit(GOTO, bexpr.falseLabel) }
     *
     * <bexpr> -> >=
     *            { expr1.op = NONE }
     *            <expr1>
     *            <expr1
     *            { expr2.op = NONE }
     *            <expr2>
     *            { emit(IF_CMPGE, bexpr.trueLabel) }
     *            { emit(GOTO, bexpr.falseLabel) }
     *
     * <bexpr> -> ==
     *            { expr1.op = NONE }
     *            <expr1>
     *            { expr2.op = NONE }
     *            <expr2>
     *            { emit(IF_CMPEQ, bexpr.trueLabel) }
     *            { emit(GOTO, bexpr.falseLabel) }
     *
     * <bexpr> -> <>
     *            { expr1.op = NONE }
     *            <expr1>
     *            { expr2.op = NONE }
     *            <expr2>
     *            { emit(IP_ICMPNE, bexpr.trueLabel) }
     *            { emit(GOTO, bexpr.falseLabel) }
     *
     * <bexpr> -> &&
     *            { bexpr1.trueLabel = newLabel() }
     *            { bexpr1.falseLabel = bexpr.falseLabel }
     *            <bexpr1>
     *            { emitLabel(bexpr1.trueLabel }
     *            { bexpr2.trueLabel = bexpr.trueLabel }
     *            { bexpr2.falseLabel = bexpr.falseLabel }
     *            <bexpr2>
     *
     * <bexpr> -> ||
     *            { bexpr1.trueLabel = bexpr.trueLabel }
     *            { bexpr1.falseLabel = newLabel() }
     *            <bexpr1>
     *            { emitLabel(bexpr1.falseLabel) }
     *            { bexpr2.trueLabel = bexpr.trueLabel }
     *            { bexpr2.falseLabel = bexpr.falseLabel }
     *            <bexpr2>
     *
     * <bexpr> -> !
     *            { bexpr1.trueLabel = bexpr.falseLabel }
     *            { bexpr1.falseLabel = bexpr.trueLabel }
     *            <bexpr1>
     */
    private void bexpr(int trueLabel, int falseLabel) {
        switch (look.tag) {
            case Tag.RELOP:
                Word relop = (Word) look;
                match(Tag.RELOP);
                expr(Op.NONE);
                expr(Op.NONE);
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
            case Tag.AND:
                match(Tag.AND);
                int bexpr1TrueLabel = code.newLabel();
                bexpr(bexpr1TrueLabel, falseLabel);
                code.emitLabel(bexpr1TrueLabel);
                bexpr(trueLabel, falseLabel);
                break;
            case Tag.OR:
                match(Tag.OR);
                int bexpr1FalseLabel = code.newLabel();
                bexpr(trueLabel, bexpr1FalseLabel);
                code.emitLabel(bexpr1FalseLabel);
                bexpr(trueLabel, falseLabel);
                break;
            case '!':
                match('!');
                bexpr(falseLabel, trueLabel);
                break;
            default:
                error("bexpr");
        }
    }

    /*
     * <expr> -> + (
     *           { exprlist.op = ADD }
     *           <exprlist> )
     *           { emitOpIfIn(op, { PRINT }) }
     *
     * <expr> -> -
     *           { expr1.op = NONE }
     *           <expr1>
     *           { expr2.op = NONE }
     *           <expr2>
     *           { emit(ISUB) }
     *           { emitOpIfIn(op, { PRINT }) }
     *
     * <expr> -> * (
     *           { exprlist.op = MUL }
     *           <exprlist> )
     *           { emitOpIfIn(op, { PRINT }) }
     *
     * <expr> -> /
     *           { expr1.op = NONE }
     *           <expr1>
     *           { expr2.op = NONE }
     *           <expr2>
     *           { emit(IDIV) }
                 { emitOpIfIn(op, { PRINT }) }
     *
     * <expr> -> NUM
     *           { emit(LDC, NUM) }
     *           { emitOpIfIn(op, { PRINT }) }
     *
     * <expr> -> ID
     *           { emit(ILOAD, lookup(ID)) }
     *           { emitOpIfIn(op, { PRINT }) }
     */
    private void expr(Op op) {
        switch (look.tag) {
            case '+': {
                match('+');
                match('(');
                exprlist(Op.ADD);
                match(')');
                break;
            }
            case '-': {
                match('-');
                expr(Op.NONE);
                expr(Op.NONE);
                code.emit(OpCode.ISUB);
                break;
            }
            case '*': {
                match('*');
                match('(');
                exprlist(Op.MUL);
                match(')');
                break;
            }
            case '/':
                match('/');
                expr(Op.NONE);
                expr(Op.NONE);
                code.emit(OpCode.IDIV);
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

        code.emitOpIfIn(op, Op.PRINT);
    }

    /*
     * <exprlist> -> { expr.op = exprlist.op }
     *               <expr>
     *               { exprlistp.op = exprlist.op }
     *               <exprlistp>
     */
    private void exprlist(Op op) {
        switch (look.tag) {
            case '+':
            case '-':
            case '*':
            case '/':
            case Tag.NUM:
            case Tag.ID:
                expr(op);
                exprlistp(op);
                break;
            default:
                error("exprlist");
        }
    }

    /*
     * <exprlistp> -> ,
     *                { expr.op = exprlistp.op }
     *                <expr>
     *                { emitOpIfIn(exprlistp.op, { ADD, MUL }) }
     *                { exprlistp1.op = exprlistp.op }
     *                <exprlistp1>
     *
     * <exprlistp> -> ε
     */
    private void exprlistp(Op op) {
        switch (look.tag) {
            case ',':
                match(',');
                expr(op);
                code.emitOpIfIn(op, Op.ADD, Op.MUL);
                exprlistp(op);
                break;
            case ')':
                break;

            default: error("exprlistp");
        }
    }

    public void toJasmin() throws IOException {
        code.toJasmin();
    }

    public enum Op implements BiConsumer<CodeGenerator, Integer> {

        NONE((code, address) -> {}),

        READ((code, address) -> {
            code.emit(OpCode.INVOKESTATIC, 0);
            code.emit(OpCode.ISTORE, address);
        }),

        PRINT((code, ignored) ->
            code.emit(OpCode.INVOKESTATIC, 1)
        ),

        ASSIGN((code, address) ->  {
            code.emit(OpCode.DUP);
            code.emit(OpCode.ISTORE, address);
        }),

        ASSIGN_LAST((code, address) ->
            code.emit(OpCode.ISTORE, address)
        ),

        ADD((code, ignored) ->
            code.emit(OpCode.IADD)
        ),

        MUL((code, ignored) ->
            code.emit(OpCode.IMUL)
        );

        private final BiConsumer<CodeGenerator, Integer> action;

        Op(BiConsumer<CodeGenerator, Integer> action) {
            this.action = action;
        }

        @Override
        public void accept(CodeGenerator codeGenerator, Integer operand) {
            action.accept(codeGenerator, operand);
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            Translator translator = new Translator(lex, br);
            translator.prog();
            translator.toJasmin();
            System.out.println("Input OK");
            br.close();
        } catch (SyntaxError | IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}