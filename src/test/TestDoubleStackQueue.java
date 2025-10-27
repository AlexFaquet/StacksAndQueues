package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import common.AbstractFactoryClient;
import common.QueueEmptyException;
import common.QueueFullException;
import impl.DoubleStackQueue;
import interfaces.IQueue;

/**
 * Tests double stack queue implementation.
 */
public class TestDoubleStackQueue extends AbstractFactoryClient {

    private static final int DEFAULT_MAX_SIZE = 10;

    /**
     * Tests that the factory constructs a non-null object.
     */
    @Test
    public void factoryReturnsNonNullDoubleStackQueue() {
        IQueue queue = getFactory().makeDoubleStackQueue(DEFAULT_MAX_SIZE);
        assertNotNull(queue, "Failure: IFactory.makeDoubleStackQueue returns null, expected non-null object");
    }

    // ---------- helpers ----------
    private static List<Object> dequeueAll(IQueue q) throws QueueEmptyException {
        List<Object> out = new ArrayList<>();
        while (!q.isEmpty()) {
            out.add(q.dequeue());
        }
        return out;
    }

    private static void enqueueMany(IQueue q, int fromInclusive, int toInclusive) throws QueueFullException {
        for (int i = fromInclusive; i <= toInclusive; i++) {
            q.enqueue(i);
        }
    }

    // ========== BASIC BEHAVIOR ==========

    /**
     * Basic FIFO test with even capacity.
     * @throws Exception
     */
    @Test
    void fifoBasicEven() throws Exception {
        IQueue q = new DoubleStackQueue(10); // halves 5 + 5
        enqueueMany(q, 1, 5);
        assertEquals(5, q.size());
        assertEquals(1, q.dequeue());
        assertEquals(2, q.dequeue());
        assertEquals(3, q.dequeue());
        assertEquals(4, q.dequeue());
        assertEquals(5, q.dequeue());
        assertTrue(q.isEmpty());
    }

    /**
     * Basic FIFO test with odd capacity.
     * @throws Exception
     */
    @Test
    void transferOncePerBatch() throws Exception {
        IQueue q = new DoubleStackQueue(10);
        enqueueMany(q, 1, 5);
        // First dequeue triggers transfer, then subsequent dequeues pop from output directly
        for (int i = 1; i <= 5; i++) {
            assertEquals(i, q.dequeue());
        }
        assertTrue(q.isEmpty());
    }

    /**
     * Interleaving enqueues and dequeues preserves FIFO order.
     */
    @Test
    void interleavedOps() throws Exception {
        IQueue q = new DoubleStackQueue(10);
        q.enqueue("A");
        q.enqueue("B");
        assertEquals("A", q.dequeue());
        q.enqueue("C");
        q.enqueue("D");
        assertEquals("B", q.dequeue());
        q.enqueue("E");
        // Now queue contains (in FIFO): C, D, E
        assertEquals("C", q.dequeue());
        assertEquals("D", q.dequeue());
        assertEquals("E", q.dequeue());
        assertTrue(q.isEmpty());
    }

    // ========== EXCEPTIONS ==========

    /**
     * Dequeue from an empty queue throws QueueEmptyException.
     */
    @Test
    void dequeueEmptyThrows() {
        IQueue q = new DoubleStackQueue(6);
        assertThrows(QueueEmptyException.class, q::dequeue);
    }

    /**
     * clear() empties the queue and allows subsequent reuse.
     */
    @Test
    void clearAllowsReuse() throws Exception {
        IQueue q = new DoubleStackQueue(10);
        enqueueMany(q, 1, 6);
        assertEquals(6, q.size());
        q.clear();
        assertTrue(q.isEmpty());
        // Reuse after clear
        q.enqueue("X");
        q.enqueue("Y");
        assertEquals("X", q.dequeue());
        assertEquals("Y", q.dequeue());
        assertTrue(q.isEmpty());
    }

    // ========== CAPACITY / FULLNESS (EVEN) ==========

    /**
     * Can fill to full capacity when total size is even and FIFO is preserved.
     */
    @Test
    void fullEvenCapacity() throws Exception {
        // With maxSize=10, halves are 5 and 5.
        // We should be able to enqueue 10 items with no dequeues:
        // - fill input to 5
        // - next enqueue triggers transfer to output (since output is empty)
        // - then fill input again to 5
        IQueue q = new DoubleStackQueue(10);
        enqueueMany(q, 1, 10); // should not throw
        assertEquals(10, q.size());

        // Dequeue all should return 1..10 in order
        for (int i = 1; i <= 10; i++) {
            assertEquals(i, q.dequeue());
        }
        assertTrue(q.isEmpty());
    }

    // ========== CAPACITY / FULLNESS (ODD) ==========

    /**
     * Tests fullness behaviour when the total capacity is odd and no dequeues occur.
     */
    @Test
    void fullOddCapacityWithoutDequeues() throws Exception {
        // With maxSize = 9, halves become 4 each (due to integer division in DoubleStack),
        // leaving one slot unused in the middle.
        // Without any dequeues, the maximum number of enqueues we can reach is 8.
        IQueue q = new DoubleStackQueue(9);

        // Enqueue 8 should be fine…
        enqueueMany(q, 1, 8);
        assertEquals(8, q.size());

        // 9th enqueue (without dequeues) should throw, because input is full and output not empty
        assertThrows(QueueFullException.class, () -> q.enqueue(9));

        // Dequeue a couple and ensure FIFO
        assertEquals(1, q.dequeue());
        assertEquals(2, q.dequeue());

        assertThrows(QueueFullException.class, () -> q.enqueue(9));
        assertThrows(QueueFullException.class, () -> q.enqueue(10));

        // Now drain output (3,4)
        assertEquals(3, q.dequeue());
        assertEquals(4, q.dequeue());

        // With output empty, we can accept new enqueues again:
        q.enqueue(9);
        q.enqueue(10);

        // Drain the rest in order
        assertEquals(5, q.dequeue());
        assertEquals(6, q.dequeue());
        assertEquals(7, q.dequeue());
        assertEquals(8, q.dequeue());
        assertEquals(9, q.dequeue());
        assertEquals(10, q.dequeue());
    }

    // ========== TRANSFER LOGIC DETAILS ==========

    /**
     * Ensures transfer from input to output occurs only when output is empty.
     */
    @Test
    void transferOnlyWhenNeeded() throws Exception {
        IQueue q = new DoubleStackQueue(10);
        // Enqueue 1..5, then dequeue twice (1,2), output now has [3,4,5]
        enqueueMany(q, 1, 5);
        assertEquals(1, q.dequeue()); // transfer happened here
        assertEquals(2, q.dequeue()); // direct pop from output

        // Enqueue 6,7,8 (go to input only; no transfer because output not empty)
        enqueueMany(q, 6, 8);

        // Dequeue remaining: first finish output side (3,4,5) then transfer input (6,7,8)
        assertEquals(3, q.dequeue());
        assertEquals(4, q.dequeue());
        assertEquals(5, q.dequeue());

        // Output now empty; next dequeue will cause transfer and return 6
        assertEquals(6, q.dequeue());
        assertEquals(7, q.dequeue());
        assertEquals(8, q.dequeue());
        assertTrue(q.isEmpty());
    }

    /**
     * size() correctly reflects items across both internal stacks.
     */
    @Test
    void sizeReflectsBothStacks() throws Exception {
        IQueue q = new DoubleStackQueue(10);
        assertEquals(0, q.size());

        // Enqueue 1..4 (input has 4)
        enqueueMany(q, 1, 4);
        assertEquals(4, q.size());

        // Dequeue 1 (forces transfer then pop)
        assertEquals(1, q.dequeue());
        assertEquals(3, q.size());

        // Enqueue 5..7 (input grows again)
        enqueueMany(q, 5, 7);
        assertEquals(6, q.size()); // output has 3 items (2,3,4), input has 3 items (5,6,7)

        // Dequeue all remaining
        List<Object> out = dequeueAll(q);
        List<Object> expected = List.of(2, 3, 4, 5, 6, 7);
        assertEquals(expected, out);
        assertEquals(0, q.size());
        assertTrue(q.isEmpty());
    }

    // ========== ROBUSTNESS / EDGE ==========

    /**
     * Draining the queue then attempting to dequeue throws QueueEmptyException.
     */
    @Test
    void drainThenThrow() throws Exception {
        IQueue q = new DoubleStackQueue(6);
        enqueueMany(q, 10, 14); // 5 items
        assertEquals(List.of(10, 11, 12, 13, 14), dequeueAll(q));
        assertTrue(q.isEmpty());
        assertThrows(QueueEmptyException.class, q::dequeue);
    }

    /**
     * clear() is idempotent and queue remains usable after multiple clears.
     */
    @Test
    void multipleClears() throws Exception {
        IQueue q = new DoubleStackQueue(10);
        enqueueMany(q, 1, 3);
        q.clear();
        q.clear(); // idempotent
        assertTrue(q.isEmpty());

        enqueueMany(q, 7, 9);
        assertEquals(3, q.size());
        assertEquals(7, q.dequeue());
        q.clear();
        assertTrue(q.isEmpty());
    }

    /**
     * Enqueue must fail when input is full and output still contains items.
     */
    @Test
    void enqueueFailsWhenOutputHasDataAndInputIsFull() throws Exception {
        var q = new impl.DoubleStackQueue(10); // 5+5 halves
        // Fill input
        for (int i = 1; i <= 5; i++) {
            q.enqueue(i);
        }
        // Partially drain to move some into output (transfer), then leave some there
        assertEquals(1, q.dequeue()); // transfer happens
        assertEquals(2, q.dequeue()); // output now holds [3,4,5]
        // Refill input to full again
        q.enqueue(6); q.enqueue(7); q.enqueue(8); q.enqueue(9); q.enqueue(10);
        // Input is full (5), output not empty (3 items) -> no transfer allowed -> enqueue must fail
        assertThrows(QueueFullException.class, () -> q.enqueue(11));
    }

    /**
     * Repeated interleaving over many rounds preserves FIFO ordering.
     */
    @Test
    void longInterleavingRemainsFifo() throws Exception {
        var q = new impl.DoubleStackQueue(6); // 3+3 halves
        for (int round = 0; round < 20; round++) {
            q.enqueue(round * 3); q.enqueue(round * 3 + 1); q.enqueue(round * 3 + 2);
            assertEquals(round * 3, q.dequeue());
            q.enqueue(1000 + round);
            assertEquals(round * 3 + 1, q.dequeue());
            assertEquals(round * 3 + 2, q.dequeue());
            assertEquals(1000 + round, q.dequeue());
            assertTrue(q.isEmpty());
        }
    }

    /**
     * Ensures size never exceeds the reachable capacity when total size is odd.
     */
    @Test
    void sizeNeverExceedsReachableCapacityOdd() throws Exception {
        var q = new impl.DoubleStackQueue(9); // reachable=8 without dequeues
        for (int i = 0; i < 8; i++) {
            q.enqueue(i);
        }
        assertEquals(8, q.size());
        // Next enqueue must fail (no room to transfer because output not empty)
        assertThrows(QueueFullException.class, () -> q.enqueue(999));
    }


    /**
     * Verifies enqueueing null values is allowed and they dequeue as null.
     */
    @Test
    void enqueueNullIsAllowedAndDequeuedAsNull() throws Exception {
        IQueue q = new impl.DoubleStackQueue(4);
        q.enqueue(null);
        q.enqueue("X");
        assertEquals(null, q.dequeue());
        assertEquals("X", q.dequeue());
    }



}
