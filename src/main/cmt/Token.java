import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Token {
    private static String[] definword = {"int","float","double","void"};


    public  boolean isDefine(String str){
        if(str == null || str.length() == 0){
            return false;
        }
        else if(find(definword,str)){
            return true;
        }
        return false;
    }


    public boolean find(String[] arr,String str){
        for (String s:arr){
            if(s.equals(str)){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {

    }

}
