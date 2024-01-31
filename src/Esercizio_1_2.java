public final class Esercizio_1_2 {

    public static boolean scan(String s) {
        int state = 0;
        int i = 0;

        while (state >= 0 && i < s.length()) {
            final char c = s.charAt(i++);
            final int _state = state;
            state = -1;

            switch (_state) {
                case 0:
                    if (c == '_')
                        state = 1;
                    else if (Character.isAlphabetic(c))
                        state = 2;
                    else if (Character.isDigit(c))
                        state = 3;
                    break;
                case 1:
                    if (c == '_')
                        state = 1;
                    else if (Character.isLetterOrDigit(c))
                        state = 2;
                    break;
                case 2: // same as below
                case 3:
                    if (c == '_' || Character.isLetterOrDigit(c))
                        // state is left unchanged, q2 always goes back to q2, same for q3
                        state = _state;
                    break;
            }
        }

        return state == 2;
    }

    public static void main(String[] args) {
        System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }

}