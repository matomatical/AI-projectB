/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.matomatical.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;

/** An extension of java's LinkedHashSet to implement the Queue interface
 *  and therefore support O(1) removal or peeking at first element
 *  (while still offering O(1) time instert/contains/delete element operations)
 * @author Matt Farrugia [farrugiam@student.unimelb.edu.au]
 * @param <E>
 */
@SuppressWarnings("serial")
public class QueueHashSet<E> extends LinkedHashSet<E> implements Queue<E> {

	/** Create a Queue over a {@link LinkedHashSet} using
	 *  {@link LinkedHashSet#LinkedHashSet(java.util.Collection)}
	 **/
	public QueueHashSet(List<E> edges) {
		super(edges);
	}
	/** Create a Queue over a {@link LinkedHashSet} using
	 *  {@link LinkedHashSet#LinkedHashSet()}
	 **/
	public QueueHashSet() {
		super();
	}

	@Override
	public boolean offer(E e) {
		super.add(e);
		return true;
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
