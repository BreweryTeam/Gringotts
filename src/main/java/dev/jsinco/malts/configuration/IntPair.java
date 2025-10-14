package dev.jsinco.malts.configuration;

import dev.jsinco.malts.utility.Couple;

import java.util.ArrayList;
import java.util.List;

public class IntPair extends Couple<Integer, Integer> {
    public IntPair(Integer first, Integer second) {
        super(first, second);
    }

    public boolean includes(int value) {
        return value >= this.a() && value <= this.b();
    }

    public boolean includesWith(int value, IntPair other) {
        return includes(value) || value >= other.a() && value <= other.b();
    }
    public boolean negative() {
        return this.a() < 0 || this.b() < 0;
    }

    public int difference(boolean negative) {
        int value = this.b() - this.a();
        if (negative) {
            return value * -1;
        }
        return value;
    }

    public List<Integer> inclusiveRange() {
        List<Integer> range = new ArrayList<>();
        for (int i = this.a(); i <= this.b(); i++) {
            range.add(i);
        }
        return range;
    }

    public static IntPair of(int a, int b) {
        return new IntPair(a, b);
    }

    @Override
    public String toString() {
        return String.format("IntPair(a=%d, b=%d)", this.a(), this.b());
    }
}
