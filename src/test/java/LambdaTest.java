import com.baixiao.async.AsyncCallback;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/8/6 14:52
 */
public class LambdaTest {

    @Test
    public void test1(){
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> list1 = AsyncCallback.asyncMap(list, in -> in + 1);
        System.out.println(list1);
    }
}
