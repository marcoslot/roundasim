/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A sequence of connected track ranges,
 * commonly used to represent a trajectory.
 */
public class TrackRangeSequence implements List<TrackRange1D> {

	final VehicleTrackMap trackMap;
	final List<TrackRange1D> ranges;
	final TrackMapArea1D area;

	/**
	 * @param ranges
	 */
	public TrackRangeSequence(VehicleTrackMap trackMap, List<TrackRange1D> ranges) {
		this.trackMap = trackMap;
		this.ranges = ranges;
		this.area = new TrackMapArea1D(trackMap);
		init();
	}

	/**
	 * 
	 */
	public TrackRangeSequence(VehicleTrackMap trackMap) {
		this(trackMap, new ArrayList<TrackRange1D>());
	}

	void init() {
		area.clear();

		for(TrackRange1D range : ranges) {
			area.add(range);
		}
	}

	public double getLength() {
		double length = 0.0;

		for(TrackRange1D range : ranges) {
			length += range.getLength();
		}

		return length;
	}


	public boolean intersects(TrackRange1D range) {
		return area.intersects(range);
	}

	public boolean intersects(TrackRangeSequence trajectory) {
		return area.intersects(trajectory.area);
	}

	public Double getDistanceFromStart(TrackPoint1D point) {
		if(!area.contains(point)) {
			return null;
		}

		double distance = 0.0;

		for(TrackRange1D range : ranges) {
			if(range.contains(point)) {
				distance += point.getOffset() - range.getStart();
				break;
			}

			distance += range.getLength();
		}

		return distance;
	}

	public Double getDistanceFromEnd(TrackPoint1D point) {
		if(!area.contains(point)) {
			return null;
		}

		double distance = 0.0;

		for(int i = ranges.size()-1; i >= 0; i--) {
			TrackRange1D range = ranges.get(i);
			
			if(range.contains(point)) {
				distance += range.getEnd() - point.getOffset();
				break;
			}

			distance += range.getLength();
		}

		return distance;
	}

	public TrackRangeSequence getSubSequence(double length) {
		List<TrackRange1D> newRanges = new ArrayList<TrackRange1D>();

		for(TrackRange1D range : ranges) {
			if(length <= 0) {
				break;
			} else if(range.getLength() > length) {
				TrackRange1D newRange = new TrackRange1D(
						range.getTrackID(), 
						range.getStart(), 
						range.getStart() + length);
				newRanges.add(newRange);
				break;
			} else {
				newRanges.add(range);
			}

			length -= range.getLength();
		}

		return new TrackRangeSequence(trackMap, newRanges);
	}

	public TrackRangeSequence getSequenceContainedBy(TrackMapArea1D area) {
		List<TrackRange1D> newRanges = new ArrayList<TrackRange1D>();

		for(TrackRange1D range : ranges) {
			if(!area.contains(range)) {
				// The area does not contain the range
				// but it may still intersect with it

				TrackArea1D trackArea = area.getArea(range.getTrackID());

				if(trackArea == null) {
					// No part of this range is contained by the area
					break;
				}

				TrackRange1D rangeContainingStart = trackArea.getRangeContaining(range.getStart());

				if(rangeContainingStart == null) {
					// The area does not contain the start of the range
					// The sequence is broken
					break;
				}

				// This part of the range is included in the area
				TrackRange1D includedRange = new TrackRange1D(
						range.getTrackID(),
						range.getStart(),
						rangeContainingStart.getEnd());

				newRanges.add(includedRange);

				// Since the range is not fully included, the sequence is broken
				break;
			} else {
				newRanges.add(range);
			}

		}

		return new TrackRangeSequence(trackMap, newRanges);
	}

	public TrackRangeSequence getSequenceNotContainedBy(TrackMapArea1D area) {
		List<TrackRange1D> newRanges = new ArrayList<TrackRange1D>();

		for(TrackRange1D range : ranges) {
			if(area.intersects(range)) {
				TrackArea1D trackArea = area.getArea(range.getTrackID());
				TrackRange1D rangeContainingStart = trackArea.getRangeContaining(range.getStart());

				if(rangeContainingStart != null) {
					// The area does contains the start of the range
					// The sequence is broken
					break;
				}

				TrackRange1D rangeAfterStart = trackArea.getRangeAfter(range.getStart());

				// This part of the range is not included in the area
				TrackRange1D includedRange = new TrackRange1D(
						range.getTrackID(),
						range.getStart(),
						rangeAfterStart.getStart());

				newRanges.add(includedRange);

				// Since the range is not fully excluded, the sequence is broken
				break;
			} else {
				newRanges.add(range);
			}

		}

		return new TrackRangeSequence(trackMap, newRanges);
	}

	public TrackMapArea1D getArea() {
		return area;
	}

	public boolean contains(TrackPoint1D point) {
		return area.contains(point);
	}

	public boolean contains(TrackRange1D range) {
		return area.contains(range);
	}

	public TrackPoint1D getStart() {
		return ranges.get(0).getStartPoint();
	}

	public TrackPoint1D getEnd() {
		return ranges.get(ranges.size()-1).getEndPoint();
	}

	/// LIST DELEGATE METHODS

	/**
	 * @return the ranges
	 */
	public List<TrackRange1D> getRanges() {
		return ranges;
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, TrackRange1D element) {
		area.add(element);
		ranges.add(index, element);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(TrackRange1D e) {
		area.add(e);
		return ranges.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends TrackRange1D> c) {
		for(TrackRange1D range : c) {
			area.add(range);
		}

		return ranges.addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends TrackRange1D> c) {
		for(TrackRange1D range : c) {
			area.add(range);
		}

		return ranges.addAll(index, c);
	}

	/**
	 * 
	 * @see java.util.List#clear()
	 */
	public void clear() {
		area.clear();
		ranges.clear();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return ranges.contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return ranges.containsAll(c);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return ranges.equals(o);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public TrackRange1D get(int index) {
		return ranges.get(index);
	}

	/**
	 * @return
	 * @see java.util.List#hashCode()
	 */
	public int hashCode() {
		return ranges.hashCode();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return ranges.indexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.List#iterator()
	 */
	public Iterator<TrackRange1D> iterator() {
		return ranges.iterator();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return ranges.lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<TrackRange1D> listIterator() {
		return ranges.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<TrackRange1D> listIterator(int index) {
		return ranges.listIterator(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public TrackRange1D remove(int index) {
		TrackRange1D range = ranges.remove(index);
		area.remove(range);
		return range;
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		boolean removed = ranges.remove(o);

		if(removed) {
			area.remove((TrackRange1D) o);
		}

		return removed;
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		boolean result = ranges.removeAll(c);
		init();
		return result;
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		boolean result = ranges.retainAll(c);
		init();
		return result;
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public TrackRange1D set(int index, TrackRange1D element) {
		TrackRange1D range = ranges.get(index);
		area.remove(range);
		area.add(element);
		return ranges.set(index, element);
	}

	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int size() {
		return ranges.size();
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.List#subList(int, int)
	 */
	public List<TrackRange1D> subList(int fromIndex, int toIndex) {
		return ranges.subList(fromIndex, toIndex);
	}

	/**
	 * @return
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return ranges.toArray();
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.List#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return ranges.toArray(a);
	}

	public TrackRangeSequence subSequence(double distance) {
		TrackRangeSequence result = new TrackRangeSequence(trackMap);

		boolean foundStartRange = false;
		double distanceCovered = 0.0;

		for(TrackRange1D range : ranges) {
			if(!foundStartRange) {
				if(distanceCovered + range.getLength() > distance) {
					TrackRange1D startRange = new TrackRange1D(
							range.getTrackID(),
							range.getStart() + distance - distanceCovered, 
							range.getEnd());

					result.add(startRange);

					foundStartRange = true;
				} else {
					distanceCovered += range.getLength();
				}
			} else {
				result.add(range);
			}
		}

		return result;
	}

	public TrackRangeSequence subSequence(TrackPoint1D endPoint) {
		TrackRangeSequence result = new TrackRangeSequence(trackMap);

		boolean foundStartRange = false;

		for(TrackRange1D range : ranges) {
			if(!foundStartRange) {
				if(range.contains(endPoint)) {
					TrackRange1D startRange = new TrackRange1D(endPoint, range.getEndPoint());
					result.add(startRange);
					foundStartRange = true;
				}
			} else {
				result.add(range);
			}
		}

		return result;
	}

	public TrackRangeSequence subSequenceExclusive(TrackPoint1D endPoint, double delta) {
		TrackRangeSequence result = new TrackRangeSequence(trackMap);

		boolean foundStartRange = false;

		for(TrackRange1D range : ranges) {
			if(!foundStartRange) {
				if(range.contains(endPoint)) {
					TrackRange1D startRange;

					if(range.contains(endPoint.getOffset() + delta)) {
						startRange = new TrackRange1D(range.getTrackID(), endPoint.getOffset() + delta, range.getEnd());
						result.add(startRange);
					} else {
						// Just start from the next range
					}
					foundStartRange = true;
				}
			} else {
				result.add(range);
			}
		}

		return result;
	}

	public double getDistanceTo(TrackMapArea1D mapArea) {
		double distance = 0.0;

		for(TrackRange1D range : ranges) {
			TrackArea1D boundArea = mapArea.getArea(range.getTrackID());

			if(boundArea != null && boundArea.intersects(range)) {
				distance += boundArea.getFirstIntersection(range) - range.getStart();
				break;
			}

			distance += range.getLength();
		}

		return distance;
	}



}
