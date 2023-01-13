package com.spectator;

import org.junit.Test;

import com.spectator.counter.Numbers;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NumbersUnitTest {

    @Test
    public void addVoter() {
        Numbers numbers = new Numbers();
        numbers.changeAll(1);

        assert(numbers.getDaily() == 1);
        assert(numbers.getHourly() == 1);
        assert(numbers.getTotally() == 1);
    }

    @Test
    public void deleteWithZero() {
        Numbers numbers = new Numbers();
        numbers.changeAll(-1);

        assert(numbers.getDaily() == 0);
        assert(numbers.getHourly() == 0);
        assert(numbers.getTotally() == 0);
    }

    @Test
    public void deleteVoter() {
        Numbers numbers = new Numbers();
        numbers.changeAll(1);
        numbers.changeAll(-1);

        assert(numbers.getDaily() == 0);
        assert(numbers.getHourly() == 0);
        assert(numbers.getTotally() == 0);
    }

    @Test
    public void setDaily() {
        Numbers numbers = new Numbers();
        int value = 2;
        numbers.setDaily(value);

        assert(numbers.getDaily() == value);
        assert(numbers.getHourly() == 0);
        assert(numbers.getTotally() == 0);
    }

    @Test
    public void setHourly() {
        Numbers numbers = new Numbers();
        int value = 3;
        numbers.setHourly(value);

        assert(numbers.getDaily() == 0);
        assert(numbers.getHourly() == value);
        assert(numbers.getTotally() == 0);
    }

    @Test
    public void setTotal() {
        Numbers numbers = new Numbers();
        int value = 4;
        numbers.setTotal(value);

        assert(numbers.getDaily() == 0);
        assert(numbers.getHourly() == 0);
        assert(numbers.getTotally() == value);
    }

    @Test
    public void deleteAfterSet() {
        Numbers numbers = new Numbers();
        int value = 4;
        numbers.setTotal(value);
        numbers.changeAll(-1);

        assert(numbers.getDaily() == 0);
        assert(numbers.getHourly() == 0);
        assert(numbers.getTotally() == value - 1);
    }
}