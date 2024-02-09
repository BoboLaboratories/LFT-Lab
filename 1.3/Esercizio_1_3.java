import java.text.Normalizer;

public final class Esercizio_1_3 {

    // Character.isLetter or Character.isAlphabetic accept accented letters
    private static boolean isLetter(char c) {
        return isInCaseInsensitiveBounds(c, 'A', 'Z');
    }

    private static boolean isInCaseInsensitiveBounds(char c, char lower, char upper) {
        lower = Character.toLowerCase(lower);
        upper = Character.toLowerCase(upper);
        c = Character.toLowerCase(c);
        return (lower <= c && c <= upper);
    }

    public static boolean scan(String s) {
        // this strips accents from string s
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[^\\p{ASCII}]", "");

        int state = 0;
        int i = 0;

        while (state >= 0 && i < s.length()) {
            final char c = s.charAt(i++);
            final int _state = state;
            state = -1;

            switch (_state) {
                case 0:
                    if (isLetter(c))
                        state = 3;
                    else if (Character.isDigit(c)) {
                        if (Character.getNumericValue(c) % 2 == 0)
                            state = 2;
                        else
                            state = 1;
                    }
                    break;
                case 1:
                    if (isInCaseInsensitiveBounds(c, 'A', 'K'))
                        state = 3;
                    else if(isInCaseInsensitiveBounds(c, 'L', 'Z'))
                        state = 4;
                    else if (Character.isDigit(c)) {
                        if (Character.getNumericValue(c) % 2 == 0)
                            state = 2;
                        else
                            state = 1;
                    }
                    break;
                case 2:
                    if (isInCaseInsensitiveBounds(c, 'A', 'K'))
                        state = 4;
                    else if(isInCaseInsensitiveBounds(c, 'L', 'Z'))
                        state = 3;
                    else if (Character.isDigit(c)) {
                        if (Character.getNumericValue(c) % 2 == 0)
                            state = 2;
                        else
                            state = 1;
                    }
                    break;
                case 3:
                    if (isLetter(c) || Character.isDigit(c))
                        state = 3;
                    break;
                case 4:
                    if (isLetter(c))
                        state = 4;
                    break;
            }
        }

        return state == 4;
    }

    public static void main(String[] args) {
        System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }

}