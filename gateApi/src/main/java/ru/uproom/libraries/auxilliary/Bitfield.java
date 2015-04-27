package ru.uproom.libraries.auxilliary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osipenko on 04.04.15.
 */
public class Bitfield {


    private final List<Integer> bits = new ArrayList<>();
    private int numSetBits;


    public int getNumSetBits() {
        return numSetBits;
    }

    public boolean isBit(int bitNumber) {
        if (bitNumber >> 5 > bits.size()) return false;
        return (bits.get(bitNumber >> 5) & (1 << (bitNumber & 0x1F))) != 0;
    }


    public void setBit(int bitNumber) {
        if (isBit(bitNumber)) return;
        int newSize = (bitNumber >> 5) + 1;
        while (bits.size() < newSize) bits.add(0);
        int value = bits.get(bitNumber >> 5);
        bits.set(bitNumber >> 5, value | (1 << (bitNumber & 0x1f)));
        ++numSetBits;
    }

    public void clearBit(int bitNumber) {
        if (!isBit(bitNumber)) return;
        int newSize = (bitNumber >> 5) + 1;
        while (bits.size() < newSize) bits.add(0);
        int value = bits.get(bitNumber >> 5);
        bits.set(bitNumber >> 5, value & ~(1 << (bitNumber & 0x1f)));
        --numSetBits;
    }


}
