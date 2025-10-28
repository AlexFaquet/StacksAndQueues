package impl;


import common.StackEmptyException;
import common.StackOverflowException;
import interfaces.IStack;

public class Stack implements IStack {
    private Object[] data;      //reference to the shared array
    private int top;            //count of elements in this stack
    private int start;          //where this stack begins in the array
    private int capacity;       //max number of elements allowed
    private boolean isFirst;    //true if this is the first stack in the double stack

    public Stack(Object[] sharedArray, boolean isFirst, int capacity) {
        this.data = sharedArray;
        this.top = 0;
        this.start = isFirst ? 0 : sharedArray.length - 1; //Either it's the first stack bottom at 0 or the second stack bottom at the last index
        this.capacity = capacity;
        this.isFirst = isFirst;
    }

    /**
     * Pushes an element onto the stack.
     * @param element the element to push
     * @throws StackOverflowException if the stack is full
     */
    @Override
    public void push(Object element) throws StackOverflowException {
        if (top == capacity) {
            throw new StackOverflowException();
        }

        if (isFirst) {
            data[start + top] = element; // grows left to right
        } else {
            data[start - top] = element; //grows right to left
        }
        top++;
    }

    /**
     * Pops an element from the stack.
     * @return the popped element
     * @throws StackEmptyException if the stack is empty
     */
    @Override
    public Object pop() throws StackEmptyException {
        Object poppedItem = null;
        if (top == 0) {
            throw new StackEmptyException();
        }

        top--;

        if (isFirst) {
            poppedItem = data[start + top];
            data[start + top] = null;
        } else {
            poppedItem = data[start - top];
            data[start - top] = null;
        }

        return poppedItem;
    }

    /**
     * Returns the top element without removing it.
     * @return the top element
     * @throws StackEmptyException if the stack is empty
     */
    @Override
    public Object top() throws StackEmptyException {
        if (top == 0) {
            throw new StackEmptyException();
        }
        Object topElement = null;
        if (isFirst) {
            topElement = data[start + top - 1];
        } else {
            topElement = data[start - top + 1];
        }
        return topElement;
    }

    /**
     * Returns the number of elements in the stack.
     * @return the size of the stack
     */
    @Override
    public int size() {
        return top;
    }

    /**
     * Checks if the stack is empty.
     * @return true if the stack is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return top == 0;
    }

    /**
     * Clears the stack.
     */
    @Override
    public void clear() {
        if (isFirst) {
            for (int i = 0; i < top; i++) {
                data[start + i] = null;
            }
        } else {
            for (int i = 0; i < top; i++) {
                data[start - i] = null;
            }
        }
        top = 0;
    }

}
