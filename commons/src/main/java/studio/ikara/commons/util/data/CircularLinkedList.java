package studio.ikara.commons.util.data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import lombok.Getter;

@Getter
public class CircularLinkedList<T> {

    private DoublePointerNode<T> head;
    private DoublePointerNode<T> tail;

    private int size = 0;

    @SafeVarargs
    public CircularLinkedList(T... elements) {
        Arrays.stream(elements).forEach(this::add);
    }

    public CircularLinkedList(Collection<T> data) {
        data.forEach(this::add);
    }

    public synchronized boolean add(T element) {

        if (head == null) {
            head = tail = new DoublePointerNode<>(element);
            head.next = tail;
            head.prev = tail;
        } else if (head == tail) {
            tail = new DoublePointerNode<>(head, element, head);
            head.next = tail;
            head.prev = tail;
        } else {
            DoublePointerNode<T> node = new DoublePointerNode<>(tail, element, head);
            tail.next = node;
            head.prev = node;
            tail = node;
        }
        size++;
        return true;
    }

    public Object[] toArray() {
        Object[] array = new Object[size];

        if (size == 0) return array;

        int i = 0;
        DoublePointerNode<T> node = this.head;
        do {
            array[i++] = node.item;
            node = node.next;

        } while (node != this.head);

        return array;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray(T[] array) {
        if (array.length < size)
            array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);

        if (size == 0) return array;

        int i = 0;
        DoublePointerNode<T> node = this.head;
        do {
            array[i++] = node.item;
            node = node.next;
        } while (node != this.head);

        if (array.length > size) array[size] = null;

        return array;
    }

    public boolean addAll(Collection<? extends T> elements) {
        boolean result = false;
        for (T element : elements) result |= this.add(element);
        return result;
    }

    public void clear() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
}
