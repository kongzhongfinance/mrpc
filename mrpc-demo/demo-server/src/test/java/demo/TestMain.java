package demo;

import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import lombok.Data;

import java.util.Optional;

/**
 * @author biezhi
 *         2017/5/21
 */
public class TestMain {

    @Data
    static class Book {
        String           title;
        Optional<String> subTitle;
    }

    public static void main(String[] args) throws SerializeException {
        Book book = new Book();
        book.setTitle("Oliver Twist");
        book.setSubTitle(Optional.of("The Parish Boy's Progress"));

        String result = JacksonSerialize.toJSONString(book);
        System.out.println(result);

        Book book1 = JacksonSerialize.parseObject(result, Book.class);
        System.out.println(book1);
    }

}
