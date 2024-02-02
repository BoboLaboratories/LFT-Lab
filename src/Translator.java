import java.io.*;

public class Translator {

    public void prog() {        
	// ... completare ...
        int lnext_prog = code.newLabel();
        statlist(lnext_prog);
        code.emitLabel(lnext_prog);
        match(Tag.EOF);
        try {
        	code.toJasmin();
        }
        catch(IOException e) {
        	System.out.println("IO error\n");
        };
	// ... completare ...
    }

    public void stat( /* completare */ ) {
        switch(look.tag) {
	// ... completare ...
            case Tag.READ:
                match(Tag.READ);
                match('(');
	        idlist(/* completare */);
                match(')');
	// ... completare ...
        }
     }

    private void idlist(/* completare */) {
        switch(look.tag) {
	    case Tag.ID:
        	int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr==-1) {
                    id_addr = count;
                    st.insert(((Word)look).lexeme,count++);
                }
                match(Tag.ID);
	// ... completare ...
    	}
    }

    private void expr( /* completare */ ) {
        switch(look.tag) {
	// ... completare ...
            case '-':
                match('-');
                expr();
                expr();
                code.emit(OpCode.ISUB);
                break;
	// ... completare ...
        }
    }

// ... completare ...
}
