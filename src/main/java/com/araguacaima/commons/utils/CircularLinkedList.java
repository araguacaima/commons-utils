package com.araguacaima.commons.utils;

/**
 * Created by Alejandro on 24/02/2015.
 */

public class CircularLinkedList {
    private ListNode actualElement = null;
    private ListNode head = null;
    private int index = 0;
    private int numberOfElements = 0;

    public boolean deleteActualElement() {
        if (index > 0) {
            numberOfElements--;
            index--;
            ListNode listNode = head;
            while (!listNode.next.equals(actualElement))
                listNode = listNode.next;
            listNode.next = actualElement.next;
            actualElement = listNode;
            return true;
        } else {
            actualElement = head.next;
            index = 0;
            return deleteFirst();
        }
    }

    public boolean deleteFirst() {
        if (isEmpty())
            return false;
        if (index > 0)
            index--;
        head = head.next;
        numberOfElements--;
        return true;
    }

    public boolean isEmpty() {
        return (numberOfElements == 0);
    }

    public Object getNext() {
        if (goToNextElement()) {
            return getActualElementData();
        } else {
            return null;
        }
    }

    public boolean goToNextElement() {
        if (isEmpty())
            return false;
        index = (index + 1) % numberOfElements;
        if (index == 0)
            actualElement = head;
        else
            actualElement = actualElement.next;
        return true;
    }

    public Object getActualElementData() {
        return actualElement.data;
    }

    public void setActualElementData(Object data) {
        actualElement.data = data;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void insert(Object value) {
        insertAfterActual(value);
    }

    public void insertAfterActual(Object data) {
        if (actualElement == null) {
            insertFirst(data);
            goToNextElement();
        } else {
            actualElement.next = new ListNode(data, actualElement.next);
            numberOfElements++;
        }
    }

    public void insertFirst(Object data) {
        if (!(isEmpty())) {
            index++;
        }
        head = new ListNode(data, head);
        numberOfElements++;
    }

    static class ListNode {
        public Object data;
        public ListNode next;

        public ListNode(Object data, ListNode next) {
            this.next = next;
            this.data = data;
        }
    }
}

