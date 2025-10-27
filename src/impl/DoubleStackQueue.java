package impl;

import interfaces.IQueue;
import interfaces.IDoubleStack;
import interfaces.IStack;
import common.QueueEmptyException;
import common.QueueFullException;
import common.StackEmptyException;
import common.StackOverflowException;

public class DoubleStackQueue implements IQueue {
    private IDoubleStack doubleStack;
    private IStack inputStack;
    private IStack outputStack;
    private int capacity;

    public DoubleStackQueue(int maxSize) {
        doubleStack = new DoubleStack(maxSize);
        inputStack = doubleStack.getFirstStack();
        outputStack = doubleStack.getSecondStack();
        capacity = 2 * (maxSize / 2);
    }

    @Override
    public void enqueue(Object element) throws QueueFullException {
        if (size() == capacity) {
            throw new QueueFullException();
        }
        try {
            inputStack.push(element);
        } catch (StackOverflowException e) {
            //Input half is full. If output is empty, transfer to free space.
            if (outputStack.isEmpty()) {
                transferInputToOutput();
                try {
                    inputStack.push(element);
                } catch (StackOverflowException again) {
                    throw new QueueFullException();
                }
        } else {
            throw new QueueFullException();
            }
        }
    }

    @Override
    public Object dequeue() throws QueueEmptyException {
        //if both stacks are empty, queue is empty.
        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            throw new QueueEmptyException();
        }


        if (outputStack.isEmpty()) {
            transferInputToOutput();
            if (outputStack.isEmpty()) {
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

    private void transferInputToOutput() {
        //Move all items from input to output (reverses order)
        while (!inputStack.isEmpty()) {
            try {
                Object x = inputStack.pop();
                outputStack.push(x);
            } catch (StackEmptyException e) {
                //input became empty; done
                break;
            } catch (StackOverflowException e) {
                //output half is full; stop transferring
                break;
            }
        }
    }
}
