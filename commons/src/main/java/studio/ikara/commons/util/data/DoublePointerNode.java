package studio.ikara.commons.util.data;

import lombok.Getter;

@Getter
public class DoublePointerNode<E> {

    E item;
    DoublePointerNode<E> next;
    DoublePointerNode<E> prev;

    public DoublePointerNode(E item) {
        this.item = item;
    }

    public DoublePointerNode(DoublePointerNode<E> prev, E element, DoublePointerNode<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
