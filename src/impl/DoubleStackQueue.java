package impl;

import interfaces.IQueue;
import interfaces.IDoubleStack;
import interfaces.IStack;
import common.QueueEmptyException;
import common.QueueFullException;

public class DoubleStackQueue implements IQueue {
    private IDoubleStack doubleStack;
    private IStack inputStack;
    private IStack outputStack;
    private int capacity;
     

    public DoubleStackQueue(int maxSize) {
        doubleStack = new DoubleStack(maxSize);
        inputStack = doubleStack.getFirstStack();
        outputStack = doubleStack.getSecondStack();
        capacity = maxSize;
    }

    @Override
    public void enqueue(Object element) throws QueueFullException {
        if (inputStack.size() == capacity) {
            throw new QueueFullException();
        }
        
        try {
            inputStack.push(element);
        }
    }

    @Override
    public Object dequeue() throws QueueEmptyException {
        //if both stacks are empty, queue is empty.
        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            throw new QueueEmptyException();
        }


        if (outputStack.isEmpty()) {
            try {
                while (!inputStack.isEmpty()) {
                    outputStack.push(inputStack.pop());
                }
            } catch (Exception e) {
                //These should not happen if my logic is correct,
                //but we handle them safely.
                throw new QueueEmptyException();
            }
        }

        try {
            return outputStack.pop();
        } catch (Exception e) {
            //Again, this should not happen but we'll rethrow a queue-level exception
            throw new QueueEmptyException();
        }
    }

    @Override
    public int size() {
        return (outputStack.size() + inputStack.size());
    }

    @Override
    public boolean isEmpty() {
        return (inputStack.isEmpty() && outputStack.isEmpty());
    }

    @Override
    public void clear() {
        inputStack.clear();
        outputStack.clear();
    }
}
