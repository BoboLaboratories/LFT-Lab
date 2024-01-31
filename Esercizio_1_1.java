public final class Esercizio_1_1 {

    public static boolean scan(String s) {
        int state = 0;
        int i = 0;

        while (state >= 0 && i < s.length()) {
            final char c = s.charAt(i++);
            final int _state = state;
            state = -1;

            switch (_state) {
                case 0:
                    if (c == '0')
                        state = 1;
                    else if (c == '1')
                        state = 0;
                    break;
                case 1:
                    if (c == '0')
                        state = 2;
                    else if (c == '1')
                        state = 0;
                    break;
                case 2:
                    if (c == '0')
                        state = 3;
                    else if (c == '1')
                        state = 0;
                    break;
                case 3:
                    if (c == '0' || c == '1')
                        state = 3;
                    break;
            }
        }

        return state != 3;
    }

    public static void main(String[] args) {
        System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }

}