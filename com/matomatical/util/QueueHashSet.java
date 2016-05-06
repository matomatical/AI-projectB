package com.matomatical.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("serial")
public class QueueHashSet<E> extends LinkedHashSet<E> implements Queue<E> {

	public QueueHashSet(List<E> edges) {
		super(edges);
	}

	public QueueHashSet() {
		super();
	}

	@Override
	public boolean offer(E e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E remove() {
		Iterator<E> it = this.iterator();
		
		if (!it.hasNext()) {
			throw new RuntimeException("Empty!");
		}
		
		E removed = it.next();
		it.remove();
		return removed;
		
	}

	@Override
	public E poll() {
		Iterator<E> it = this.iterator();
		
		if (!it.hasNext()) {
			return null;
		}
		
		E removed = it.next();
		it.remove();
		return removed;
	}

	@Override
	public E element() {
		Iterator<E> it = this.iterator();
		
		if (!it.hasNext()) {
			throw new RuntimeException("Empty!");
		}
		
		return it.next();
	}

	@Override
	public E peek() {
		Iterator<E> it = this.iterator();
		
		if (!it.hasNext()) {
			return null;
		}
		
		return it.next();
	}

}
