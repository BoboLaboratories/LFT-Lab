public final class Word extends Token {

    public static final Word ASSIGN  = new Word(Tag.ASSIGN, "assign");
    public static final Word TO      = new Word(Tag.TO,     "to");
    public static final Word IFTOK   = new Word(Tag.IF,     "if");
    public static final Word ELSETOK = new Word(Tag.ELSE,   "else");
    public static final Word DOTOK   = new Word(Tag.DO,     "do");
    public static final Word FORTOK  = new Word(Tag.FOR,    "for");
    public static final Word BEGIN   = new Word(Tag.BEGIN,  "begin");
    public static final Word END     = new Word(Tag.END,    "end");
    public static final Word PRINT   = new Word(Tag.PRINT,  "print");
    public static final Word READ    = new Word(Tag.READ,   "read");
    public static final Word INIT    = new Word(Tag.INIT,   ":=");
    public static final Word OR      = new Word(Tag.OR,     "||");
    public static final Word AND     = new Word(Tag.AND,    "&&");
    public static final Word LT      = new Word(Tag.RELOP,  "<");
    public static final Word GT      = new Word(Tag.RELOP,  ">");
    public static final Word EQ      = new Word(Tag.RELOP,  "==");
    public static final Word LE      = new Word(Tag.RELOP,  "<=");
    public static final Word NE      = new Word(Tag.RELOP,  "<>");
    public static final Word GE      = new Word(Tag.RELOP,  ">=");

    private final String lexeme;

    public Word(int tag, String lexeme) {
        super(tag);
        this.lexeme = lexeme;
    }

    public String getLexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
        return "<" + tag + ", " + lexeme + ">";
    }
}