public final class NumberTok extends Token {

    private final String lexeme;

    public NumberTok(String lexeme) {
        super(Tag.NUM);
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