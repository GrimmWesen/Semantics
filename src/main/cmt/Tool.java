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
