public final class Esercizio_1_4 {

    public static boolean scan(String s) {
        int state = 0;
        int i = 0;

        while (state >= 0 && i < s.length()) {
            final char c = s.charAt(i++);
            final int _state = state;
            state = -1;
            
            switch (_state) {
                case 0:
                    if (c == '.')
                        state = 3;
                    else if (c == '+' || c == '-')
                        state = 1;
                    else if (c == '0' || c == 'e')
                        state = 10;
                    else if (Character.isDigit(c))  // will never be 0
                        state = 2;
                    break;
                case 1:
                    if (c == '.')
                        state = 3;
                    else if (c == '0' || c == 'e' || c == '+' || c == '-')
                        state = 10;
                    else if (Character.isDigit(c)) // will never be 0
                        state = 2;
                    break;
                case 2:
                    if (c == 'e')
                        state = 5;
                    else if (c == '.')
                        state = 3;
                    else if (c == '+' || c == '-')
                        state = 10;
                    else if (Character.isDigit(c))
                        state = 2;
                    break;
                case 3:
                    if (c == 'e' || c == '+' || c == '-' || c == '.')
                        state = 10;
                    else if (Character.isDigit(c))
                        state = 4;
                    break;
                case 4:
                    if (c == 'e')
                        state = 5;
                    else if (c == '+' || c == '-' || c == '.')
                        state = 10;
                    else if (Character.isDigit(c))
                        state = 4;
                    break;
                case 5:
                    if (c == '.')
                        state = 8;
                    if (c == '+' || c == '-')
                        state = 6;
                    else if (c == '0' || c == 'e')
                        state = 10;
                    else if (Character.isDigit(c))  // will never be 0
                        state = 7;
                    break;
                case 6:
                    if (c == '.')
                        state = 8;
                    else if (c == '0' || c == 'e' || c == '+' || c == '-')
                        state = 10;
                    else if (Character.isDigit(c))  // will never be 0
                        state = 7;
                    break;
                case 7:
                    if (c == '.')
                        state = 8;
                    else if (c == 'e' || c == '+' || c == '-')
                        state = 10;
                    else if (Character.isDigit(c))
                        state = 7;
                    break;
                case 8:
                    if (c == 'e' || c == '+' || c == '-' || c == '.')
                        state = 10;
                    else if (Character.isDigit(c))
                        state = 9;
                    break;
                case 9:
                    if (c == 'e' || c == '+' || c == '-')
                        state = 10;
                    else if (Character.isDigit(c))
                        state = 9;
                    break;
            }
        }

        return state == 2 || state == 4 || state == 7 || state == 9;
    }

    public static void main(String[] args) {
        System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }

}