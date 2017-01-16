import java.util.Random;

/**
 * Created by biezhi on 2016/12/3.
 */
public class RTest {
    public static void main(String[] args) {
        Random randomGenerator = new Random();
        for(int i=0; i<1000;i ++){
            System.out.println(randomGenerator.nextInt(2));
        }
    }
}
