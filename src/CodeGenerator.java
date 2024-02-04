import java.util.LinkedList;
import java.io.*;

public final class CodeGenerator {

    private final LinkedList<Instruction> instructions = new LinkedList<>();

    private int label = 0;

    public void emit(OpCode opCode) {
        instructions.add(new Instruction(opCode));
    }

    public void emit(OpCode opCode, int operand) {
        instructions.add(new Instruction(opCode, operand));
    }

    public void emitOpIfIn(Translator.Op op, Translator.Op... ops) {
        emitOpIfIn(op, -1, ops);
    }

    /*
     * Definiamo la funzione emitOpIfIn(e, p, S), i cui argomenti sono:
     *   - e ∈ Translator.Op, un emettitore di codice
     *   - p ∈ int, un argomento da passare a op
     *   - S ⊆ Translator.Op, un insieme di emettitori di codici
     *
     * ∀ x ∈ S, x(p) ⟺ (x = e) ∨ ((e = ASSIGN) ∧ (x = ASSIGN_LAST))
     *
     * x(p) ⟺ e ∈ S ∨ ((e = ASSIGN) ∧ (x = ASSIGN_LAST))
     *
     */
    public void emitOpIfIn(Translator.Op op, int operand, Translator.Op... ops) {
        for (Translator.Op operation : ops) {
            if (op == operation || (op == Translator.Op.ASSIGN && operation == Translator.Op.ASSIGN_LAST)) {
                operation.accept(this, operand);
            }
        }
    }

    public void emitLabel(int operand) {
        emit(OpCode.LABEL, operand);
    }

    public int newLabel() {
        return label++;
    }

    public void toJasmin() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter("Output.j"));
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER);
        while (!instructions.isEmpty()) {
            Instruction instruction = instructions.remove();
            sb.append(instruction.toJasmin());
        }
        sb.append(FOOTER);
        out.println(sb);
        out.flush();
        out.close();
    }

    private static final String HEADER = ".class public Output \n"
            + ".super java/lang/Object\n"
            + "\n"
            + ".method public <init>()V\n"
            + " aload_0\n"
            + " invokenonvirtual java/lang/Object/<init>()V\n"
            + " return\n"
            + ".end method\n"
            + "\n"
            + ".method public static print(I)V\n"
            + " .limit stack 2\n"
            + " getstatic java/lang/System/out Ljava/io/PrintStream;\n"
            + " iload_0 \n"
            + " invokestatic java/lang/Integer/toString(I)Ljava/lang/String;\n"
            + " invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n"
            + " return\n"
            + ".end method\n"
            + "\n"
            + ".method public static read()I\n"
            + " .limit stack 3\n"
            + " new java/util/Scanner\n"
            + " dup\n"
            + " getstatic java/lang/System/in Ljava/io/InputStream;\n"
            + " invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V\n"
            + " invokevirtual java/util/Scanner/next()Ljava/lang/String;\n"
            + " invokestatic java/lang/Integer.parseInt(Ljava/lang/String;)I\n"
            + " ireturn\n"
            + ".end method\n"
            + "\n"
            + ".method public static run()V\n"
            + " .limit stack 1024\n"
            + " .limit locals 256\n";

    private static final String FOOTER = " return\n"
            + ".end method\n"
            + "\n"
            + ".method public static main([Ljava/lang/String;)V\n"
            + " invokestatic Output/run()V\n"
            + " return\n"
            + ".end method\n";

}
