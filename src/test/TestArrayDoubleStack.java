package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import common.AbstractFactoryClient;
import interfaces.IDoubleStack;
import interfaces.IStack;

/**
 * Tests array double stack implementation.
 */
public class TestArrayDoubleStack extends AbstractFactoryClient {

    private static final int DEFAULT_MAX_SIZE = 10;
    private IDoubleStack doubleStack;
    private IStack firstStack;
    private IStack secondStack;

    /**
     * Tests that the factory constructs a non-null double stack.
     */
    @Test
    public void factoryReturnsNonNullDoubleStackObject() {
        IDoubleStack doubleStack1 = getFactory().makeDoubleStack(DEFAULT_MAX_SIZE);
        assertNotNull(doubleStack1, "Failure: IFactory.makeDoubleStack returns null, expected non-null object");
    }

    @BeforeEach
    public void setUp() {
        doubleStack = getFactory().makeDoubleStack(DEFAULT_MAX_SIZE);
        firstStack = doubleStack.getFirstStack();
        secondStack = doubleStack.getSecondStack();
    }



    @Test
    public void bothStacksAreInitiallyEmpty() {
        assertTrue(firstStack.isEmpty(), "First stack should be empty initially");
        assertTrue(secondStack.isEmpty(), "Second stack should be empty initially");
    }

    @Test
    public void pushAndPopFirstStack() throws Exception {
        firstStack.push("A");
        assertEquals(1, firstStack.size());
        assertEquals("A", firstStack.top());
        assertEquals("A", firstStack.pop());
        assertTrue(firstStack.isEmpty());
    }

    @Test
    public void pushAndPopSecondStack() throws Exception {
        secondStack.push("B");
        assertEquals(1, secondStack.size());
        assertEquals("B", secondStack.top());
        assertEquals("B", secondStack.pop());
        assertTrue(secondStack.isEmpty());
    }

    @Test
    public void stacksDoNotInterfere() throws Exception {
        firstStack.push("A");
        secondStack.push("B");
        assertEquals("A", firstStack.top());
        assertEquals("B", secondStack.top());
        assertEquals("A", firstStack.pop());
        assertEquals("B", secondStack.pop());
    }

    @Test
    public void cannotExceedHalfCapacity() throws Exception {
        for (int i = 0; i < DEFAULT_MAX_SIZE / 2; i++) {
            firstStack.push(i);
        }
        assertThrows(common.StackOverflowException.class, () -> firstStack.push("overflow"));
        for (int i = 0; i < DEFAULT_MAX_SIZE / 2; i++) {
            secondStack.push(i);
        }
        assertThrows(common.StackOverflowException.class, () -> secondStack.push("overflow"));
    }

    
}
