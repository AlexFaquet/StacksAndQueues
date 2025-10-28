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
        doubleStack = new DoubleStack(2 * maxSize);
        inputStack = doubleStack.getFirstStack();
        outputStack = doubleStack.getSecondStack();
        capacity = maxSize;
    }

    /**
     * Enqueues an element at the back of the queue.
     * @param element the element to add
     * @throws QueueFullException if the queue is full
     */
    @Override
    public void enqueue(Object element) throws QueueFullException {
        if (size() == capacity) {
            throw new QueueFullException();
        }
        try {
            inputStack.push(element);
        } catch (StackOverflowException impossible) {
            // With 2*Q internal and the size guard, this shouldn't happen.
            // You can either keep this as defensive or replace with an assert.
            throw new QueueFullException();
        }
    }

    /**
     * Dequeues an element from the front of the queue.
     * @return the dequeued element
     * @throws QueueEmptyException if the queue is empty
     */
    @Override
    public Object dequeue() throws QueueEmptyException {
        // Empty queue if both stacks are empty
        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            throw new QueueEmptyException();
        }

        // If output is empty, pour input -> output to restore FIFO
        if (outputStack.isEmpty()) {
            while (!inputStack.isEmpty()) {
                try {
                    outputStack.push(inputStack.pop());
                    //In these exceptions, I am constrained by the interface to throw only QueueEmptyException.
                    //So for cases where I need to throw StackEmptyException or StackOverflowException, I catch them and re-throw as QueueEmptyException.
                } catch (StackEmptyException e) {
                    throw new QueueEmptyException(); // Defensive; shouldn't happen
                } catch (StackOverflowException e) {
                    throw new QueueEmptyException(); // Defensive; shouldn't happen
                }
            }
            // If both were empty, still empty
            if (outputStack.isEmpty()) {
                throw new QueueEmptyException();
            }
        }

        try {
            return outputStack.pop();
        } catch (StackEmptyException e) {
            // Defensive: output said non-empty but pop failed
            throw new QueueEmptyException();
        }
    }



    /**
     * Returns the number of elements in the queue.
     * @return the size of the queue
     */
    @Override
    public int size() {
        return (outputStack.size() + inputStack.size());
    }

    /**
     * Checks if the queue is empty.
     * @return true if the queue is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return (inputStack.isEmpty() && outputStack.isEmpty());
    }

    /**
     * Clears the queue.
     */
    @Override
    public void clear() {
        inputStack.clear();
        outputStack.clear();
    }

}
