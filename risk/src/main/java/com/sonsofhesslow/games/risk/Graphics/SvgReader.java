package com.sonsofhesslow.games.risk.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;

import com.sonsofhesslow.games.risk.graphics.Geometry.Beizier;
import com.sonsofhesslow.games.risk.graphics.Geometry.BeizierPath;
import com.sonsofhesslow.games.risk.graphics.Geometry.BeizierPathBuilder;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;

/**
 * Created by Daniel on 20/04/2016.
 */
class SvgReader
{
    // we need to seriously speed this up.
    // probably using the raw input stream and byte arrays. Pretending this is c here I come!
    public SvgReader(InputStream inputStream)
    {
        r = new PushbackReader(new BufferedReader(new InputStreamReader(inputStream)),100);
    }
    private Vector2 pos = Vector2.Zero();
    private final PushbackReader r;

    private float readFloat() throws IOException {
        //fast minimal float stream parsing.
        float ret=0;
        final float[] table = {0.1f,0.01f,0.001f,0.0001f,0.00001f};
        int decimal = -1;
        boolean negative= false;
        for(int i=0;i<20;++i) {
            int c = (char)r.read();
            if(c == -1)break;
            if(c == '.'){decimal = 0; continue;}
            if(c == '-'){negative = true; continue;}
            int num = c-48;
            if(num>=0&&num<10) { //is digit
                if(decimal>=0)
                {
                    ret += num*table[decimal];
                    ++decimal;
                    if(decimal>=table.length)break;
                }
                else
                {
                    ret = ret * 10 + num;
                }
            } else{
                r.unread(c);
                break;
            }
        }
        skipDigit();
        return negative ? -ret : ret;
    }



    private void skipWhite() throws IOException {
        for(;;) {
            int c = r.read();
            if(!Character.isWhitespace(c)|| c == -1) {
                r.unread(c);
                break;
            }
        }
    }


    private void skipDigit() throws IOException {
        for(;;) {
            int c = r.read();
            if(!Character.isDigit(c)|| c == -1) {
                r.unread(c);
                break;
            }
        }
    }

    private Vector2 readVector2(boolean relative) throws IOException {
        skipWhite();
        float x = readFloat();
        skipWhite();
        int comma = r.read();
        if(comma != ',') throw new FileFormatException("file format mismatch");
        skipWhite();
        float y = readFloat();
        if(relative)
            return Vector2.Add(new Vector2(x,y),pos);
        else return new Vector2(x,y);
    }

    private Beizier readBeiz(char mode) throws IOException{
        switch (mode) {
            case 'm':
                if(pos.x != 0 || pos.y != 0) {return readBeiz('l');}
                pos = readVector2(true);
                return null;
            case 'M':
                if(pos.x != 0 || pos.y != 0) {return readBeiz('L');}
                pos = readVector2(false);
                return null;
            case 'C': {
                Vector2 start = pos;
                Vector2 c1 = readVector2(false);
                Vector2 c2 = readVector2(false);
                pos = readVector2(false);
                return new Beizier(start, c1, c2, pos);
            }
            case 'c':
            {
                Vector2 start = pos;
                Vector2 c1 = readVector2(true);
                Vector2 c2 = readVector2(true);
                pos = readVector2(true);
                return new Beizier(start, c1, c2, pos);
            }
            case 'L':
            {
                Vector2 start = pos;
                pos = readVector2(false);
                return new Beizier(start, start, pos, pos);
            }
            case 'l':
            {
                Vector2 start = pos;
                pos = readVector2(true);
                return new Beizier(start, start, pos, pos);
            }
            default:
                throw new RuntimeException("unknown/unimplemented mode:\'" + mode + "\'");
        }
    }

    class FileFormatException extends RuntimeException{
        FileFormatException(String message) {
            super(message);
        }
    }

    //todo attributes may come in any order.
    //don't assuse that d is last.... it's not.

    public SVGPath readPath() throws IOException
    {
        // parsing a path,
        // [starts here] ...noise... <path  ...data... /> [ends here]
        if(!advancePast("<path")) return null;
        boolean isDashed=false;
        boolean isCont=false;
        boolean isReg =false;

        BeizierPath ret = null;
        for(;;)
        {
            skipWhite();
            String s = readWord();
            if(s.length()==0)return null;
            advancePast('=');
            advancePast('"');
            switch (s) {
                case "d":
                    pos = Vector2.Zero();
                    BeizierPathBuilder b = new BeizierPathBuilder();

                    for (; ; ) {
                        skipWhite();
                        int c = (char) r.read();
                        if (c == -1) break;
                        if (c == '\"') {
                            ret = b.get(false);
                            r.unread('\"'); //we'll advance past later on. need to keep our return state consitant.
                            break;
                        }
                        if (c == 'z' || c == 'Z') {
                            ret = b.get(true);
                            break;
                        }
                        while (isNextFloat()) {
                            Beizier beiz = readBeiz((char) c);
                            if (beiz != null)
                                b.addBeiz(beiz);
                        }
                    }
                    if (ret == null) {
                        throw new FileFormatException("no data in the path->d tag");
                    }
                    break;
                case "id":
                    String id = readWord();
                    if (id.equals("cont")) isCont = true;
                    if (id.equals("reg")) isReg = true;
                    break;
                case "style":
                    for (; ; ) {
                        skipWhite();
                        String attr = readWord(":\"");
                        if (attr.equals("stroke-dasharray")) {
                            r.read();//reads the :
                            skipWhite();
                            if (isNextFloat()) {
                                isDashed = true;
                            }
                            break;
                        }
                        skipWhite();
                        readWord(";\"");
                        int p = peek();
                        if (p == -1 || p == '\"') break;
                        r.read(); // reads the ;
                    }
                    break;
            }
            advancePast('"');
            skipWhite();
            if(peek(2).equals("/>"))break;
            if(peek()==-1)break;
        }
        advancePast("/>");
        return new SVGPath(ret,isDashed,isReg,isCont);
    }

    private int peek() throws IOException{
        int c = r.read();
        if(c!=-1)
            r.unread((char)c);
        return c;
    }

    private String peek(int length) throws IOException{
        char buffer[] = new char[length];
        for(int i = 0; i<length;i++) {
            buffer[i] = (char) r.read();
        }
        r.unread(buffer);
        return new String(buffer,0,length);
    }

    private boolean isNextFloat() throws IOException
    {
        skipWhite();
        char c = (char) peek();
        return Character.isDigit(c)||c == '-'||c=='.';
    }

    private boolean advancePast(String string) throws IOException
    {
        //could be waaaay faster. optimize if needed.
        int current_char=0;
        char[] s = string.toCharArray();
        for(;;)
        {
            int c = r.read();
            if(c == s[current_char]) {
                if(++current_char==s.length) return true;
            }
            else{
                current_char = 0;
            }
            if(c ==-1) return false;
        }
    }

    private boolean advancePast(char s) throws IOException {
        for(;;)
        {
            int c = r.read();
            if(c==-1)return false;
            if((char )c== s)return true;
        }
    }

    private String readWord() throws IOException
    {
        char[] s = new char[20]; // we don't need any higher precision than that.
        int i = 0;
        for(;i<20;++i) {
            int c = (char)r.read();
            if(c == -1)break;
            if(Character.isLetter(c)) {
                s[i] =(char) c;
            } else{
                r.unread(c);
                break;
            }
        }
        return new String(s,0,i);
    }

    private String readWord(String delimiter) throws IOException
    {
        char[] s = new char[20]; // we don't need any higher precision than that.
        int i = 0;
        for(;i<20;++i) {
            int c = (char)r.read();
            if(c == -1)break;
            if(delimiter.indexOf(c)==-1) {
                s[i] =(char) c;
            } else{
                r.unread(c);
                break;
            }
        }
        return new String(s,0,i);
    }
}
