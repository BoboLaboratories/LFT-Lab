import java.io.*;

public class Lexer {

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

    private Token error(char prev) {
        System.err.println("Erroneous character after " + prev + " : " + peek);
        return null;
    }

    public Token scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
            if (peek == '\n') {
                line++;
            }
            readChar(br);
        }

        switch (peek) {
            case '!':
                return reset(Token.NOT);
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
            case '+':
                return reset(Token.PLUS);
            case '-':
                return reset(Token.MINUS);
            case '*':
                return reset(Token.MULT);
            case '/':
                switch (readChar(br)) {
                    case '/':
                        while (peek != '\n' && peek != '\r' && peek != EOF) {
                            readChar(br);
                        }
                        return scan(br);
                    case '*':
                        while (peek != '/') {
                            do {
                                readChar(br);
                            } while (peek != '*');
                            readChar(br);
                        }
                        readChar(br); // consumes last '/'
                        return scan(br);
                    default:
                        return Token.DIV;
                }
            case ';':
                return reset(Token.SEMICOLON);
            case ',':
                return reset(Token.COMMA);
            case '&':
                return (readChar(br) == '&') ? reset(Word.AND) : error('&');
            case '|':
                return (readChar(br) == '|') ? reset(Word.OR) : error('|');
            case '=':
                return (readChar(br) == '=') ? reset(Word.OR) : error('=');
            case '<':
                switch (readChar(br)) {
                    case '=':
                        return reset(Word.LE);
                    case '>':
                        return reset(Word.NE);
                    default:
                        return Word.LT;
                }
            case '>':
                if (readChar(br) == '=') {
                    return reset(Word.GE);
                } else {
                    return Word.GT;
                }
            case EOF:
                return new Token(Tag.EOF);
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
                        System.err.println("Erroneous char sequence " + lexeme);
                        return null;
                    }

                    switch (lexeme) {
                        case "assign": return Word.ASSIGN;
                        case "begin":  return Word.BEGIN;
                        case "print":  return Word.PRINT;
                        case "else":   return Word.ELSETOK;
                        case "read":   return Word.READ;
                        case "for":    return Word.FORTOK;
                        case "end":    return Word.END;
                        case "to":     return Word.TO;
                        case "if":     return Word.IFTOK;
                        case "do":     return Word.DOTOK;
                        default:       return new Word(Tag.ID, lexeme);
                    }
                } else if (Character.isDigit(peek)) {
                    do {
                        sb.append(peek);
                        readChar(br);
                    } while (Character.isDigit(peek));
                    String lexeme = sb.toString();
                    return new NumberTok(lexeme);
                } else {
                    System.err.println("Erroneous character: " + peek);
                    return null;
                }
        }
    }

}
