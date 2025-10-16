package impl;


import common.StackEmptyException;
import common.StackOverflowException;
import interfaces.IStack;

public class Stack implements IStack {
    private Object[] data;      //reference to the shared array
    private int top;            //index of the top element
    private int start;          //where this stack begins in the array
    private int capacity;       //max number of elements allowed
    private boolean isFirst;    //true if this is the first stack in the double stack

    public Stack(Object[] sharedArray, boolean isFirst, int capacity) {
        this.data = sharedArray;
        this.top = 0;
        this.start = isFirst ? 0 : sharedArray.length-1; //Either it's the first stack bottom at 0 or the second stack bottom at the last index
        this.capacity = capacity;
        this.isFirst = isFirst;
    }

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

    @Override
    public Object pop() throws StackEmptyException {
        Object poppedItem = null;
        if (top == 0) {
            throw new StackEmptyException();
        }

        top--;

        if (isFirst) {
            poppedItem = data[start+top];
            data[start + top] = null;
        } else {
            poppedItem = data[start - top];
            data[start - top] = null;
        }

        return poppedItem;
    }

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

    @Override
    public int size() {

        return top;
    }

    @Override
    public boolean isEmpty() {
        return top == 0;
    }

    @Override
    public void clear() {
        top = 0;
    }


    
}
