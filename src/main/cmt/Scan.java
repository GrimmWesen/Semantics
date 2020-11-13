
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

public class Scan {
    public static char[] origin;
    public static char[] done;
    public static String[] linecode;
    public List<String> zoneInfo = new ArrayList<String>(); // main#0_8
    public List<Integer> funcIndex = new ArrayList<Integer>();

    public int endIndex = -1;


    public Map<String,Object>[] zone = null;  //每个域的变量

    public Token t = new Token();
    public Tool to = new Tool();

    public  void read(){
        try{
            String sourceAddr = "./src/main/resources/code.txt";
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

        zone = new Map[zoneInfo.size()]; //几个函数几个域
        for (int i=0;i<zoneInfo.size();i++){
            zone[i] = new HashMap<String, Object>();
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

    public int isFuncUse(int lineID){
        String str = linecode[lineID];
        for (String info:zoneInfo){
            String funcName = info.split("#")[0];
            if(str.contains(funcName) && str.contains("(") && str.contains(";") && !funcIndex.contains(lineID)){
                return Integer.parseInt(info.split("#")[1]);
            }
        }
        return -1;
    }
    public boolean isSingle(String line){
        String copy = new String(line);
        String noemp = copy.replace(" ","");
        if(noemp.length()==1 && noemp.equals("}")){
            return true;
        }
        return false;
    }


    public void analysis(int start){
        Stack<String> stack = new Stack<String>();
        Stack<String> stackwhile = new Stack<String>();
        Stack<String> stackFor = new Stack<String>();


        stack.push("0_无");

        for(int i = start;;i++){
            String line = linecode[i];
            if(stack.size() == 0){
                break;
            }
            else if(line.length()>5 && line.contains("return")){  //return
                System.out.print(line+"\t\t\t\t\t//");
                String[] strs = stack.peek().split("_"); //"1_identy"
                int zoneID = where(Integer.parseInt(strs[0])); //在哪个域被调用的
                String identy = strs[1]; //在那个域的变量名称
                if(!identy.equals("无")){  //需要返回值
                    String str = line.trim().split(" ")[1].replace(";","");//取字符
                    Object res = to.value(str,zone[where(i)]).toString(); //取值
                    zone[zoneID].put(identy,res);

                    System.out.println("返回"+identy+" ="+res);
                }
                stack.pop();
                if(stack.size() == 0){break;}
                i = Integer.parseInt(strs[0]); //回到调用处。由于+1 到下一行
            }
            else if(isFuncUse(i)!= -1){  //函数调用
                System.out.print(line+"\t\t\t\t\t//");
                if(line.contains("=")){
                    String left = line.split("=")[0];
                    String define = left.split(" ")[0]; //类型
                    String identy = left.split(" ")[1]; // 变量名称
                    stack.push(i+"_"+identy);
                }
                else{
                    //f()  没有赋值
                }

                //参数复制
                String argss = line.substring(line.indexOf("(")+1,line.indexOf(")")); //传了哪几个参数
                String[] args = argss.split(",");//参数的列表（1,2,ab）

                int tt = isFuncUse(i);//函数声明在哪行
                int funcZone = where(tt); //调用的函数在哪个域
                String funcline = linecode[tt]; //函数声明那行
                String fargss = funcline.substring(funcline.indexOf("(")+1,funcline.indexOf(")")); //传了哪几个参数
                String[] fargs = fargss.split(",");//参数的列表

                for (int j = 0; j < fargs.length ; j++) {
                    String can = fargs[j].split(" ")[1]; //arr[] \len
                    to.transValue(can,args[j],zone[where(i)], zone[funcZone]);
                }
                System.out.println("\n");
                i = tt-1; //-1确保加1后，到达函数声明界面。

            }
            else if(funcIndex.contains(i)){ //函数
                System.out.println("###########################进入函数"+zoneInfo.get(where(i))+"###############################");
            }
            else if(line.contains("int") && !funcIndex.contains(i) && !t.hasXun(line)){ //声明变量并赋值
                System.out.print(line+"\t\t\t\t\t//");
                if (!line.contains("=")){
                    System.out.println("声明未赋值");
                    continue;
                }
                String[] lr = line.trim().split("=");
                String l = lr[0];
                String r = lr[1].replace(" ", "").replace(";", "");
                String[] ls = l.split(" ");
                String id = ls[1];

                if (r.contains("{")) {
                    String[] arr = r.substring(1, r.length() - 1).split(",");
                    int cnt = 0;
                    for (String s : arr) {
                        String aN = id.substring(0, id.length() - 2);
                        String arrN = aN + "[" + cnt + "]";
                        zone[where(i)].put(arrN, s);
                        cnt++;
                    }
                    System.out.println(id+"="+r);
                }
                else {
                    String res = to.alog(r, zone[where(i)]);
                    zone[where(i)].put(id, res);
                    System.out.println(id+"="+res);
                }

            }
            else if(!line.contains("==")&&line.contains("=") &&!line.contains("int") &&!t.hasXun(line)){
                System.out.print(line +"\t\t\t\t\t//");
                String[] lr = line.replace(" ","").split("=");
                String des = lr[0];
                String r = lr[1];
                if(des.contains("[")){ //b[i]
                    String istr = des.substring(des.indexOf("[")+1,des.indexOf("]"));
                    String aName = des.substring(0,des.indexOf("["));
                    String inum = to.value(istr,zone[where(i)]).toString();
                    des = aName+"["+inum+"]";
                }
                String res = to.alog(r,zone[where(i)]);
                zone[where(i)].put(des,res);
                System.out.println(des+"="+res);
            }
            else if(line.contains("if")){
                //寻找结束}
                System.out.print (line +"\t\t\t\t\t//");
                Stack<Integer> sif = new Stack<Integer>();
                sif.push(1);
                int be = i;
                while (sif.size()!=0){
                    be++;
                    String line_in_if = linecode[be];
                    if(line_in_if.contains("{")){
                        sif.push(1);
                    }
                    if(line_in_if.contains("}")){
                        sif.pop();
                    }
                }
                int endIFindex = be;
                String condition = line.substring(line.indexOf("if")+3,line.indexOf("{")-1); //括号里的条件
                boolean conRes = to.judge(condition,zone[where(i)]);
                System.out.println("---→("+condition +") =" +conRes);

                if(!conRes){
                    i = endIFindex;
                }
            }
            else if(line.contains("while")){
                //寻找结束}
                System.out.print (line +"\t\t\t\t\t//");
                Stack<Integer> sWhi = new Stack<Integer>();
                sWhi.push(1);
                int be = i;
                while (sWhi.size()!=0){
                    be++;
                    String line_in_while = linecode[be];
                    if(line_in_while.contains("{")){
                        sWhi.push(1);
                    }
                    if(line_in_while.contains("}")){
                        sWhi.pop();
                    }
                }
                int endIFindex = be;
                stackwhile.push(i+"_"+endIFindex);
                String condition = line.substring(line.indexOf("while")+6,line.indexOf("{")-1); //括号里的条件
                boolean conRes = to.judge(condition,zone[where(i)]);
                System.out.println("---→"+condition +" =" +conRes);

                if(!conRes){
                    stackwhile.pop();
                    i = endIFindex;
                }
            }
            else if(line.contains("for")){
                System.out.print (line +"\t\t\t\t\t//");
                int be = i;
                int endIFindex = -1;
                boolean flag = false;
                if(stackFor.size()==0){ //没有过 for
                    flag = true;
                }
                else{ //有了是外层的for
                    String[] str = stackFor.peek().split("_");
                    if(Integer.parseInt(str[0]) != i){
                        flag = true;
                    }
                }
                if(flag){
                    Stack<Integer> sstemp = new Stack<Integer>();
                    sstemp.push(1);
                    while (sstemp.size() != 0) {
                        be++;
                        String line_in_while = linecode[be];
                        if (line_in_while.contains("{")) {
                            sstemp.push(1);
                        }
                        if (line_in_while.contains("}")) {
                            sstemp.pop();
                        }
                    }
                    endIFindex = be;
                    stackFor.push(i + "_" + endIFindex);
                }

                String[] three = line.substring(line.indexOf("(")+1,line.lastIndexOf(")")).split(";");
                String ini = three[0];
                String condi = three[1];
                String move = three[2];


                if(flag){ //c初始int i =0;
                    String id = ini.split("=")[0].split(" ")[1];
                    String val = ini.substring(ini.indexOf("=")+1);
                    zone[where(i)].put(id,val);
                    System.out.print("赋值："+ini+"; 条件: (");
                }
                else {
                    String moveid = move.substring(0,move.length()-2);
                    String moveres = to.alog(move,zone[where(i)]);
                    zone[where(i)].put(moveid,moveres);
                }
                boolean condiRes = to.judge(condi,zone[where(i)]);
                System.out.println(")---→"+condiRes);
                if(!condiRes){
                    String s= stackFor.pop();
                    int end = Integer.parseInt(s.split("_")[1]);
                    i = end;
                }


            }
            else if(isSingle(line)){
                System.out.println(line+"\t\t\t\t\t//");
//                if(stackif.size()!=0){
//                    i = stackif.pop()-1;
//                }
               if(stackwhile.size()!=0){
                    String info = stackwhile.peek();
                    String[] infos = info.split("_");
                    int from = Integer.parseInt(infos[0]);
                    int end = Integer.parseInt(infos[1]);
                    if(i == end && stackwhile.size()!=0){
                        stackwhile.pop();
                        i =from-1;
                    }
                }
                if (stackFor.size()!=0){
                   String info = stackFor.peek();
                   String[] infos = info.split("_");
                   int from = Integer.parseInt(infos[0]);
                   int end = Integer.parseInt(infos[1]);
                   if(i == end && stackFor.size()!=0){
//                       stackFor.pop();
                       i =from-1;
                   }
               }

            }

        }

    }


    public static void main(String[] args) {
        Scan sc = new Scan();
        sc.read();
        sc.flitter();
        sc.findFunc();
        sc.analysis(0);

    }
}
