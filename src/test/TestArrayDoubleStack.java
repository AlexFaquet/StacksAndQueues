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
    /**
     * Sets up a fresh double stack and its two component stacks before each test.
     */
    @BeforeEach
    public void setUp() {
        doubleStack = getFactory().makeDoubleStack(DEFAULT_MAX_SIZE);
        firstStack = doubleStack.getFirstStack();
        secondStack = doubleStack.getSecondStack();
    }

    /**
     * Verifies both stacks start empty.
     */
    @Test
    public void bothStacksAreInitiallyEmpty() {
        assertTrue(firstStack.isEmpty(), "First stack should be empty initially");
        assertTrue(secondStack.isEmpty(), "Second stack should be empty initially");
    }

    /**
     * Pushes and pops on the first stack to check basic LIFO behaviour.
     */
    @Test
    public void pushAndPopFirstStack() throws Exception {
        firstStack.push("A");
        assertEquals(1, firstStack.size());
        assertEquals("A", firstStack.top());
        assertEquals("A", firstStack.pop());
        assertTrue(firstStack.isEmpty());
    }

    /**
     * Pushes and pops on the second stack to check basic LIFO behaviour.
     */
    @Test
    public void pushAndPopSecondStack() throws Exception {
        secondStack.push("B");
        assertEquals(1, secondStack.size());
        assertEquals("B", secondStack.top());
        assertEquals("B", secondStack.pop());
        assertTrue(secondStack.isEmpty());
    }

    /**
     * Ensures operations on one stack do not affect the other.
     */
    @Test
    public void stacksDoNotInterfere() throws Exception {
        firstStack.push("A");
        secondStack.push("B");
        assertEquals("A", firstStack.top());
        assertEquals("B", secondStack.top());
        assertEquals("A", firstStack.pop());
        assertEquals("B", secondStack.pop());
    }

    /**
     * Confirms each stack cannot exceed half of the allocated capacity.
     */
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

    /**
     * Verifies that popping or peeking an empty stack throws StackEmptyException.
     */
    @Test
    void popOnEmptyThrows() {
        var ds = getFactory().makeDoubleStack(8);
        var a = ds.getFirstStack();
        var b = ds.getSecondStack();
        assertThrows(common.StackEmptyException.class, a::pop);
        assertThrows(common.StackEmptyException.class, b::pop);
        assertThrows(common.StackEmptyException.class, a::top);
        assertThrows(common.StackEmptyException.class, b::top);
    }

    /**
     * Checks LIFO ordering independently on both sides.
     */
    @Test
    void lifoBothSides() throws Exception {
        var s = getFactory().makeDoubleStack(10);
        var a = s.getFirstStack();
        var b = s.getSecondStack();
        a.push(1); a.push(2); a.push(3);
        b.push("x"); b.push("y");
        assertEquals(3, a.pop()); assertEquals(2, a.pop()); assertEquals(1, a.pop());
        assertEquals("y", b.pop()); assertEquals("x", b.pop());
    }

    /**
     * Ensures clear() empties the stack and it can be reused afterwards.
     */
    @Test
    void clearResetsAndAllowsReuse() throws Exception {
        var s = getFactory().makeDoubleStack(6);
        var a = s.getFirstStack();
        a.push("A"); a.push("B");
        a.clear();
        assertTrue(a.isEmpty());
        a.push("C");
        assertEquals("C", a.top());
    }

    /**
     * Verifies odd total capacity is split by floor division between stacks.
     */
    @Test
    void oddCapacityEachGetsFloorHalf() throws Exception {
        var s = getFactory().makeDoubleStack(9); // each gets 4
        var a = s.getFirstStack();
        var b = s.getSecondStack();
        for (int i = 0; i < 4; i++) {
            a.push(i);
        }
        for (int i = 0; i < 4; i++) {
            b.push(i);
        }
        assertThrows(common.StackOverflowException.class, () -> a.push(99));
        assertThrows(common.StackOverflowException.class, () -> b.push(99));
    }

}
