package com.scottlogic.deg.generator.generation;

import java.util.ArrayList;
import java.util.List;

class Node {
    private int nbrChar = 1;
    prList<Node> nextNodes = new ArrayList<>();
    private boolean isNbrMatchedStringUpdated;
    long matchedStringIdx = 0;
    private char minChar;
    private char maxChar;

    int getNbrChar() {
        return nbrChar;
    }

    void setNbrChar(int nbrChar) {
        this.nbrChar = nbrChar;
    }

    List<Node> getNextNodes() {
        return nextNodes;
    }

    void setNextNodes(List<Node> nextNodes) {
        this.nextNodes = nextNodes;
    }

    void updateMatchedStringIdx() {
        if (isNbrMatchedStringUpdated) {
            return;
        }
        if (nextNodes.size() == 0) {
            matchedStringIdx = nbrChar;
        } else {
            for (Node childNode : nextNodes) {
                childNode.updateMatchedStringIdx();
                long childNbrChar = childNode.getMatchedStringIdx();
                matchedStringIdx += nbrChar * childNbrChar;
            }
        }
        isNbrMatchedStringUpdated = true;
    }

    long getMatchedStringIdx() {
        return matchedStringIdx;
    }

    char getMinChar() {
        return minChar;
    }

    void setMinChar(char minChar) {
        this.minChar = minChar;
    }

    char getMaxChar() {
        return maxChar;
    }

    void setMaxChar(char maxChar) {
        this.maxChar = maxChar;
    }
}