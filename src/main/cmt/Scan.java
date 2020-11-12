
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Scan {
    public static char[] origin;
    public static char[] done;
    public static String[] linecode;
    public List<String> zoneInfo = new ArrayList<String>(); // main#0_8
    public List<Integer> funcIndex = new ArrayList<Integer>();

    public int endIndex = -1;


    public Map<String,Object>[] zone;  //每个域的变量

    public Token t = new Token();

    public  void read(){
        try{
            String sourceAddr = "./src/main/resources/p.txt";
            File file  = new File(sourceAddr);
            FileReader fr = new FileReader(file);
            int length = (int) file.length();//字节数既是字符数 ，因为char大小一个字节
            origin = new char[length+1];
            fr.read(origin);
            fr.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 保留\n,去除其他转义符
     */
    public  void flitter(){
        done = new char[origin.length];
        int i = 0;
        for(char ch:origin){
            if(ch == '\r' ||ch =='\t'){
                continue;
            }
            done[i++] = ch;
        }
        String s = new String(done);
        s = s.trim();
        linecode = s.split("\n"); //按行分割字符串
    }

    public  void findFunc(){
        int linenum = 0;
//        List<Integer> list = new ArrayList<Integer>();
        List<String> funclist = new ArrayList<String>(); // main#0  f#
        for(String str:linecode){
            if(str.equals("")){
                linenum++;
                continue;
            }
            if(str.length()>5 && str.substring(0,6).equals("return")){
                if(zoneInfo.size()==1){
                    endIndex = linenum;
                }
            }
            if(str.charAt(str.length()-1) =='{' && str.charAt(str.length()-2) == ')'){
                String[] phraser = str.split(" ");
                if(t.isDefine(phraser[0])){
                    funcIndex.add(linenum);
                    String funcname = phraser[1].substring(0,phraser[1].indexOf('('))+"#"+linenum;
                    zoneInfo.add(funcname);
                }
            }
            linenum ++;
        }

//        for(int i=0;i<list.size()-1;i++){
//            int end = list.get(i+1)-1;
//            int begin = list.get(i);
//            String info = funclist.get(i)+"#"+begin+"_"+end;
//            zoneInfo.add(info);
//        }
//        String last = funclist.get(list.size()-1)+"#"+list.get(list.size()-1)+"_"+linenum;
//        zoneInfo.add(last);

        zone = new Map[funclist.size()]; //几个函数几个域
        for(Map m:zone){
            m = new HashMap<String, Object>(); //初始化
        }
    }
    public int where(int i){
        int j = 0;
        int res = -1;
        String funcName = null;
        if(i>=funcIndex.get(funcIndex.size()-1)){
            return funcIndex.size()-1;
        }

        for(Integer in:funcIndex){
            if(in>i){
//                funcName = zoneInfo.get(j).split("#")[0];
                res = j-1;
                break;
            }
            j++;
        }
        return res;
    }

    public void analysis(int start,int back){
        int zoneid  = where(start); //判断在哪个函数
        Map<String,Object> map = zone[zoneid]; //找到对应函数的值的map
        for(int i = start;;i++){
            String line = linecode[i];
            if(i == endIndex) {
                break;
            }
            if(i!= endIndex && line.length()>5 && line.substring(0,6).equals("return")){
                analysis(back+1,0);
            }

        }

    }

    public static void main(String[] args) {
        Scan sc = new Scan();
        sc.read();
        sc.flitter();
        sc.findFunc();
        for(Integer e:sc.funcIndex){
            System.out.println(e);
        }
        System.out.println(sc.endIndex);
    }
}
