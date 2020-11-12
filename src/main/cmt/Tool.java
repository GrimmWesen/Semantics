import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.jexl2.*;
public class Tool {
//    public Map<String,Object> map = new HashMap<String, Object>();
//    private static JexlEngine jexlEngine = new Engine();

    public Object value(String s,Map<String,Object> map){
        String p = "^[+-]?(0|([1-9]\\d*))(\\.\\d+)?$";
        if(s.equals("true")){
            return true;
        }
        if(s.equals("false")){
            return false;
        }
        if(Pattern.matches(p,s)){
            return Double.parseDouble(s);
        }
        return map.get(s);
    }
    public void transValue(String s,String in,Map<String,Object> fmap,Map<String,Object> desmap){
        if(s.contains("[")){ //arr[]
            String arrName = s.substring(0,s.indexOf("[")); //arr[10]  arr
            StringBuffer sb = new StringBuffer("{");
            for (int i=0;i<100;i++){
                String ai = arrName+"["+i+"]";
                if (!fmap.containsKey(ai)){
                    break;
                }
                Object x = fmap.get(ai);
                desmap.put(ai,x);

                sb.append(x.toString());
                sb.append(",");
            }
            sb.append("}");
            System.out.print("数组传参" + arrName+"[] = "+sb.toString()+"   ");

        }
        else{
            Object x = value(in,fmap); //(len,6)
            desmap.put(s,x);
            System.out.print("普通传参"+s+"="+x.toString());
        }
    }

    public int plus(){
        return 0;
    }

    public static void main(String[] args) {
        JexlContext jc = new MapContext();
        jc.set("arr[0]", 1);
        jc.set("ans", new StringBuffer());
        Expression e = new JexlEngine().createExpression("arr[0]=1;");
        e.evaluate(jc);
        System.out.println(jc.get("arr[0"));
//        System.out.println(jc.get("a")+" "+jc.get("b")+" "+jc.get("c"));
//        e.evaluate(jc);
//        System.out.println(jc.get("a")+" "+jc.get("b")+" "+jc.get("c"));

//        JexlEngine engine = new JexlEngine();//创建表达式引擎对象
//        JexlContext context = new MapContext();//创建Context设值对象
//        String expressionStr = "for (j; j < i; j++){a = 1;}";//表达式，表达式为逻辑语句
//        context.set("i",2);
//        Expression expression = engine.createExpression(expressionStr);//使用引擎创建表达式对象
//        Object o = expression.evaluate(context);//使用表达式对象开始计算
//        System.out.println(context.get("j"));//输出：2


    }
}
