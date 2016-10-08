package nl.eur.ese.bouman.tas.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Group;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.SimpleAssistant;
import nl.eur.ese.bouman.tas.data.Slot;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;
import nl.eur.ese.bouman.tas.solver.BranchInformation;
import nl.eur.ese.bouman.tas.solver.TASolver;

public class SolverTest
{
	@Test
	public void test1()
	{
		Instance i = new Instance();

		Slot sl1 = new Slot("MO", 9, 11);
		Slot sl2 = new Slot("MO", 11, 13);
		Slot sl3 = new Slot("TU", 9, 11);
		Slot sl4 = new Slot("TU", 11, 13);
		
		Group g1 = new Group("G1");
		Group g2 = new Group("G2");
		Group g3 = new Group("G3");

		i.addGroup(g1);
		i.addGroup(g2);
		i.addGroup(g3);
		
		Session s1 = new Session(g1, sl1, "A");
		Session s2 = new Session(g2, sl1, "A");
		Session s3 = new Session(g3, sl2, "A");
		Session s4 = new Session(g3, sl3, "B");
		Session s5 = new Session(g2, sl4, "B");
		Session s6 = new Session(g1, sl4, "B");
		
		i.addSession(s1); i.addSession(s2); i.addSession(s3);
		i.addSession(s4); i.addSession(s5); i.addSession(s6);
		
		Map<Slot,Double> available = new HashMap<>();
		available.put(sl1, 0d);
		available.put(sl3, 0d);
		
		
		Assistant a1 = new SimpleAssistant("Assistant 1", available, 1);
		CostInformation ci = CostInformation.getDefault();
		BranchInformation bi = BranchInformation.getDefault();
		
		TASolver solver = new TASolver(i, a1, bi, 10);
		solver.run();
		List<AssistantSchedule> solution = solver.getSchedules();
		System.out.println(solution);
		assertEquals(solution.size(), 3);
		
		Map<Slot,Integer> slotCounts = new HashMap<>();
		Map<Group,Integer> groupCounts = new HashMap<>();
		for (AssistantSchedule as : solution)
		{
			assertEquals(as.getAssistant(), a1);
			
			System.out.println(as);
			
			if (as.getSessions().size() >= 2)
			{			
				assertEquals(0, as.getRemainingCapacity("A"));
				assertEquals(0, as.getRemainingCapacity("B"));
				assertEquals(1, as.getRemainingCapacity("C"));
			}
			else
			{
				assertEquals(1, as.getRemainingCapacity("A"));
				assertEquals(0, as.getRemainingCapacity("B"));
				assertEquals(1, as.getRemainingCapacity("C"));
			}
			
			for (Session s : as.getSessions())
			{
				slotCounts.merge(s.getSlot(), 1, Integer::sum);
				groupCounts.merge(s.getGroup(), 1, Integer::sum);
			}
		}
		
		assertEquals(2, slotCounts.get(sl1).intValue());
		assertEquals(3, slotCounts.get(sl3).intValue());
		
		assertEquals(1, groupCounts.get(g1).intValue());
		assertEquals(1, groupCounts.get(g2).intValue());
		assertEquals(3, groupCounts.get(g3).intValue());
		
		// TODO: check costs?
	}
}
