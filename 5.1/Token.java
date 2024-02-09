public class Token {

    public static final Token NOT       = new Token('!');
    public static final Token LPT       = new Token('(');
    public static final Token RPT       = new Token(')');
    public static final Token LPQ       = new Token('[');
    public static final Token RPQ       = new Token(']');
    public static final Token LPG       = new Token('{');
    public static final Token RPG       = new Token('}');
    public static final Token PLUS      = new Token('+');
    public static final Token MINUS     = new Token('-');
    public static final Token MULT      = new Token('*');
    public static final Token DIV       = new Token('/');
    public static final Token SEMICOLON = new Token(';');
    public static final Token COMMA     = new Token(',');

    protected final int tag;

    protected Token(int tag) {
        this.tag = tag;
    }

    public String getLexeme() {
        return "";
    }

    @Override
    public String toString() {
        return "<" + tag + ">";
    }

}