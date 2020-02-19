import com.gmall.util.JwtUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwtUtil {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", "1");
        map.put("nickname", "张三");

        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date());
        String encode = JwtUtil.encode("2020219gmall0105", map, ip + time);

        System.out.println(encode);
    }
}
