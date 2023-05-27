import java.io.*;
import java.util.*;


class HuffmanNode implements Comparable<HuffmanNode> {
    int frequency;
    char data;
    HuffmanNode left, right;

    public HuffmanNode(int frequency, char data) {
        this.frequency = frequency;
        this.data = data;
        left = right = null;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}
