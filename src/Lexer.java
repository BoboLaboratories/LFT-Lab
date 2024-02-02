import java.io.*;

public final class Lexer {

    private static final char EOF = (char) -1;

    private char peek = ' ';
    private int line = 1;

    private char readChar(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = EOF; // ERROR
        }
        return peek;
    }

    private Token reset(Token token) {
        peek = ' ';
        return token;
    }

    private Token erroneousChar(char prev) {
        String tmp = (peek == EOF) ? "EOF" : Character.toString(peek);
        throw new SyntaxError("erroneous character '" + tmp + "' after '" + prev + "'");
    }

    public Token scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
            if (peek == '\n') {
                line++;
            }
            readChar(br);
        }

        switch (peek) {
            // parenthesis
            case '(':
                return reset(Token.LPT);
            case ')':
                return reset(Token.RPT);
            case '[':
                return reset(Token.LPQ);
            case ']':
                return reset(Token.RPQ);
            case '{':
                return reset(Token.LPG);
            case '}':
                return reset(Token.RPG);

            // separators
            case ',':
                return reset(Token.COMMA);
            case ';':
                return reset(Token.SEMICOLON);


            // init
            case ':':
                return (readChar(br) == '=') ? reset(Word.INIT) : erroneousChar(':');

            // math operators + comments
            case '+':
                return reset(Token.PLUS);
            case '-':
                return reset(Token.MINUS);
            case '*':
                return reset(Token.MULT);
            case '/':
                switch (readChar(br)) {
                    case '/':   // single-line comments
                        while (peek != '\n' && peek != '\r' && peek != EOF) {
                            readChar(br);
                        }
                        return scan(br);
                    case '*':   // multi-line comments
                        while (peek != '/') {
                            do {
                                readChar(br);
                            } while (peek != '*' && peek != EOF);
                            if (peek == EOF) {
                                throw new SyntaxError("unclosed multi-line before end of file");
                            }
                            readChar(br); // consumes '*'
                        }
                        readChar(br); // consumes last '/'
                        return scan(br);
                    default:    // math division
                        return Token.DIV;
                }

                // boolean operators
            case '!':
                return reset(Token.NOT);
            case '&':
                return (readChar(br) == '&') ? reset(Word.AND) : erroneousChar('&');
            case '|':
                return (readChar(br) == '|') ? reset(Word.OR) : erroneousChar('|');


            // relational operations
            case '=':
                return (readChar(br) == '=') ? reset(Word.EQ) : erroneousChar('=');
            case '>':
                if (readChar(br) == '=') {
                    return reset(Word.GE);
                } else {
                    return Word.GT;
                }
            case '<':
                switch (readChar(br)) {
                    case '=':
                        return reset(Word.LE);
                    case '>':
                        return reset(Word.NE);
                    default:
                        return Word.LT;
                }

                // EOF
            case EOF:
                return new Token(Tag.EOF);

            // keywords, identifiers and numbers
            default:
                StringBuilder sb = new StringBuilder();
                if (Character.isLetter(peek) || peek == '_') {
                    boolean isAccepted = peek != '_';
                    do {
                        isAccepted |= peek != '_';
                        sb.append(peek);
                        readChar(br);
                    } while (Character.isLetterOrDigit(peek) || peek == '_');

                    String lexeme = sb.toString();
                    if (!isAccepted) {
                        throw new SyntaxError("erroneous char sequence " + lexeme);
                    }

                    switch (lexeme) {
                        case "assign":
                            return Word.ASSIGN;
                        case "begin":
                            return Word.BEGIN;
                        case "print":
                            return Word.PRINT;
                        case "else":
                            return Word.ELSETOK;
                        case "read":
                            return Word.READ;
                        case "for":
                            return Word.FORTOK;
                        case "end":
                            return Word.END;
                        case "to":
                            return Word.TO;
                        case "if":
                            return Word.IFTOK;
                        case "do":
                            return Word.DOTOK;
                        default:
                            return new Word(Tag.ID, lexeme);
                    }
                } else if (Character.isDigit(peek)) {
                    if (peek == '0') {
                        readChar(br);
                        if (Character.isDigit(peek)) {
                            erroneousChar('0');
                        } else {
                            sb.append('0');
                        }
                    } else {
                        do {
                            sb.append(peek);
                            readChar(br);
                        } while (Character.isDigit(peek));
                    }
                    String lexeme = sb.toString();
                    return new NumberTok(lexeme);
                } else {
                    throw new SyntaxError("erroneous character '" + peek + "'");
                }
        }
    }

    public int getLine() {
        return line;
    }

}
