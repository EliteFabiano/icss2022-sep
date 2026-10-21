package nl.han.ica.datastructures;

import java.util.NoSuchElementException;

public class HANStack<T> implements IHANStack<T> {
    private Node<T> top;

    @Override
    public void push(T value) {
        top = new Node<>(value, top);
    }

    @Override
    public T pop() {
        T value = peek();
        top = top.next;
        return value;
    }

    @Override
    public T peek() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        return top.value;
    }

    private static class Node<T> {
        private final T value;
        private final Node<T> next;

        private Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }
}
