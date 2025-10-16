package impl;
import interfaces.IDoubleStack;

public class DoubleStack implements IDoubleStack {
    private Stack firstStack;
    private Stack secondStack;

    public DoubleStack(int maxSize) {
        Object[] sharedArray = new Object[maxSize];
        int capacityPerStack = maxSize / 2;
        firstStack = new Stack(sharedArray, true, capacityPerStack);
        secondStack = new Stack(sharedArray, false, capacityPerStack);
    }

    @Override
    public Stack getFirstStack() {
        return firstStack;
    }

    @Override
    public Stack getSecondStack() {
        return secondStack;
    }    
}
