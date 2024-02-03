public final class Instruction {

    private final OpCode opCode;
    private final int operand;

    public Instruction(OpCode opCode) {
        this(opCode, -1);
    }

    public Instruction(OpCode opCode, int operand) {
        this.opCode = opCode;
        this.operand = operand;
    }

    public String toJasmin() {
        String code = " ";
        switch (opCode) {
            case IOR:
            case DUP:
            case POP:
            case IADD:
            case IMUL:
            case IDIV:
            case ISUB:
            case INEG:
            case IAND:
                code += "    " + opCode.name().toLowerCase() + "\n";
                break;
            case LDC:
            case ILOAD:
            case ISTORE:
                code += "    " + opCode.name().toLowerCase() + " " + operand + "\n";
                break;
            case IFNE:
            case GOTO:
            case IF_ICMPEQ:
            case IF_ICMPLE:
            case IF_ICMPLT:
            case IF_ICMPNE:
            case IF_ICMPGE:
            case IF_ICMPGT:
                code += "    " + opCode.name().toLowerCase() + " L" + operand + "\n";
                break;
            case LABEL:
                code += "L" + operand + ":\n";
                break;
            case INVOKESTATIC:
                code += opCode.name().toLowerCase();
                if (operand == 1) {
                    code += " Output/print(I)V\n";
                } else {
                    code += " Output/read()I\n";
                }
                break;
        }
        return code;
    }

}
