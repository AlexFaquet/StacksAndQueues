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
        } catch (common.StackOverflowException impossible) {
            // Should never happen under 2*Q internal + size guard:
            // turning this into a hard failure is clearer than mis-mapping to QueueFull.
            throw new IllegalStateException("Unexpected overflow in enqueue()", impossible);
        }
    }

    /**
     * Dequeues an element from the front of the queue.
     * @return the dequeued element
     * @throws QueueEmptyException if the queue is empty
     */
    @Override
    public Object dequeue() throws QueueEmptyException {
        // If both stacks are empty, the queue is empty.
        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            throw new QueueEmptyException();
        }

        // If output is empty, transfer input -> output to restore FIFO order.
        if (outputStack.isEmpty()) {
            while (!inputStack.isEmpty()) {
                Object x;
                try {
                    x = inputStack.pop(); // may throw StackEmptyException
                } catch (common.StackEmptyException e) {
                    // Inconsistent: input said non-empty; treat as internal bug.
                    throw new AssertionError("Input non-empty but pop() failed", e);
                }
                try {
                    outputStack.push(x);   // may throw StackOverflowException
                } catch (common.StackOverflowException e) {
                    // Impossible under 2*Q-internal + size()==capacity guard.
                    throw new IllegalStateException("Unexpected overflow during transfer", e);
                }
            }
            // Defensive: if nothing moved, still empty.
            if (outputStack.isEmpty()) {
                throw new QueueEmptyException();
            }
        }

        try {
            return outputStack.pop();
        } catch (common.StackEmptyException e) {
            // Defensive: output claimed non-empty but pop failed — surface queue-level empty.
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
