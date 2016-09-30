package nl.eur.ese.bouman.tas.test;

import org.junit.Test;

import nl.eur.ese.bouman.tas.data.Slot;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SlotTest
{
	@Test
	public void testOverlap()
	{
		Slot s1 = new Slot("MO", 9, 11);
		Slot s2 = new Slot("MO", 11, 13);
		assertFalse(s1.overlaps(s2));
		assertFalse(s2.overlaps(s1));
		assertTrue(s1.overlaps(s1));
		assertTrue(s2.overlaps(s2));
		
		Slot s3 = new Slot("TU", 9, 11);
		Slot s4 = new Slot("TU", 11, 13);
		assertFalse(s3.overlaps(s4));
		assertFalse(s4.overlaps(s3));
		assertTrue(s3.overlaps(s3));
		assertTrue(s4.overlaps(s4));
		
		assertFalse(s1.overlaps(s3));
		assertFalse(s2.overlaps(s4));
		assertFalse(s1.overlaps(s4));
		assertFalse(s2.overlaps(s3));
		
		Slot s5 = new Slot("MO", 9, 17);
		Slot s6 = new Slot("MO", 11, 15);

		assertTrue(s5.overlaps(s6));
		assertTrue(s6.overlaps(s5));
	}
	
	@Test
	public void testAdjacent()
	{
		Slot s1 = new Slot("MO", 9, 11);
		Slot s2 = new Slot("MO", 11, 13);
		Slot s3 = new Slot("MO", 13, 15);
		assertTrue(s1.adjacent(s2));
		assertTrue(s2.adjacent(s1));
		assertTrue(s2.adjacent(s3));
		assertTrue(s3.adjacent(s2));
		assertFalse(s1.adjacent(s3));
		assertFalse(s3.adjacent(s1));
		assertFalse(s1.adjacent(s1));
		assertFalse(s2.adjacent(s2));
		assertFalse(s3.adjacent(s3));
		
		Slot s4 = new Slot("WO", 11, 13);
		assertFalse(s1.adjacent(s4));
		assertFalse(s4.adjacent(s1));
		
		Slot s5 = new Slot("MO", 9, 15);
		assertFalse(s1.adjacent(s5));
		assertFalse(s2.adjacent(s5));
		assertFalse(s3.adjacent(s5));
		assertFalse(s5.adjacent(s1));
		assertFalse(s5.adjacent(s2));
		assertFalse(s5.adjacent(s3));
	}
	
	@Test
	public void testCompare()
	{
		List<Slot> ref = new ArrayList<>();
		ref.add(new Slot("MO", 9, 11));
		ref.add(new Slot("MO", 9, 13));
		ref.add(new Slot("MO", 11, 13));
		ref.add(new Slot("TU", 9, 11));
		ref.add(new Slot("TU", 9, 13));
		ref.add(new Slot("TU", 11, 13));
		
		List<Slot> test = new ArrayList<>(ref);
		Random r = new Random(1234);
		for (int t=0; t < 100; t++)
		{
			Collections.shuffle(test, r);
			Collections.sort(test);
			assertEquals(test, ref);
		}
	}
}
