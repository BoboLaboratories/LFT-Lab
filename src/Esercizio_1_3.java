public final class Esercizio_1_3 {

    public static boolean scan(String s) {
        int state = 0;
        int i = 0;

        while (state >= 0 && i < s.length()) {
            final char c = s.charAt(i++);
            switch (state) {
                case 0:
                    if (Character.isDigit(c))
                        state = (Character.getNumericValue(c) % 2 == 0) ? 1 : 2;
                    else
                        state = -1;
                    break;
                case 1:
                    if (Character.isDigit(c))
                        if (Character.getNumericValue(c) % 2 == 1)
                            state = 3;
                    else if ((c >= 'a' && c <= 'k') || (c >= 'A' && c <= 'K'))
                        state = 4;
                    else
                        state = -1;
                    break;
                case 2:
                    if (Character.isDigit(c))
                        if (Character.getNumericValue(c) % 2 == 0)
                            state = 2;
                    else if ((c >= 'l' && c <= 'z') || (c >= 'L' && c <= 'Z'))
                        state = 4;
                    else
                        state = -1;
                    break;
                case 4:
                    if (!Character.isLetter(c))
                        state = -1;
                    break;
            }
        }

        return state == 4;
    }

    public static void main(String[] args) {
        System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }

}