package com.gmall.cart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GmallCartWebApplicationTests {

    @Test
    void TestBigDecimal() {
        BigDecimal bigDecimal1 = new BigDecimal(0.01f);
        BigDecimal bigDecimal2 = new BigDecimal(0.01d);
        BigDecimal bigDecimal3 = new BigDecimal("0.01");

        System.out.println(bigDecimal1);
        System.out.println(bigDecimal2);
        System.out.println(bigDecimal3);

        int i = bigDecimal1.compareTo(bigDecimal2);
        System.out.println(i);

        System.out.println(bigDecimal1.add(bigDecimal2));
        System.out.println(bigDecimal1.subtract(bigDecimal2).setScale(3,
                BigDecimal.ROUND_HALF_DOWN));
        System.out.println(bigDecimal1.multiply(bigDecimal2));
        System.out.println(bigDecimal1.divide(bigDecimal2, 3
                , BigDecimal.ROUND_HALF_DOWN));

    }

}
