/**
 * @author biezhi
 *         2017/4/22
 */
public class MM {

    public void aa(int a) {

    }

    public static Class<?> getBasicType(String type) {
        return int.class;
    }

    public static void main(String[] args) {
        try {
            System.out.println(MM.class.getMethod("aa", getBasicType("int")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
