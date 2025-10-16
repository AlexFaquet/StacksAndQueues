package impl;

import interfaces.IQueue;
import interfaces.IDoubleStack;
import interfaces.IStack;
import common.QueueEmptyException;
import common.QueueFullException;

public class Queue implements IQueue {
    private IDoubleStack doubleStack;
    private IStack inputStack;
    private IStack outputStack;

    public DoubleStackQueue(int maxSize) {
        doubleStack = new DoubleStack(maxSize);
        inputStack = doubleStack.getFirstStack();
        outputStack = doubleStack.getSecondStack();
    }

    @Override
    public void enqueue(Object element) throws QueueFullException {
        
    }



    
}
